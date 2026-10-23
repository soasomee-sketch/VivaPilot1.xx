package com.iptvplayer.app

import android.app.Application
import android.content.Intent
import com.iptvplayer.app.cast.CastManager
import com.iptvplayer.app.data.CrashLogger
import com.iptvplayer.app.data.LocalStore
import com.iptvplayer.app.data.MediaKind
import com.iptvplayer.app.data.WatchProgress
import com.iptvplayer.app.player.NavQueue
import com.iptvplayer.app.player.PlayerActivity
import com.iptvplayer.app.player.QueueEntry
import com.iptvplayer.app.player.QueueKind
import com.iptvplayer.app.util.isTvDevice
import com.iptvplayer.app.billing.BillingManager

class IptvApp : Application(), coil.ImageLoaderFactory {
    /**
     * Wspólny "ładowacz" plakatów i logotypów dla całej appki (biblioteka Coil).
     *
     * Optymalizacja: domyślnie Coil respektuje nagłówki cache serwera — a spora część
     * serwerów IPTV każe NIE przechowywać obrazków, więc przy każdym przewinięciu siatki
     * te same plakaty pobierały się z sieci od nowa (wolniej i więcej danych). Tu:
     * - pamięć dyskowa do 250 MB, niezależnie od nagłówków serwera,
     * - RGB565 dla obrazków bez przezroczystości (2x mniej pamięci RAM na plakat — ważne
     *   na przystawkach TV, które mają jej mało),
     * - łagodne przenikanie przy pojawianiu się plakatu.
     * Rozmiar obrazka i tak jest dopasowywany do rozmiaru kafelka (Coil robi to sam).
     */
    override fun newImageLoader(): coil.ImageLoader =
        coil.ImageLoader.Builder(this)
            .memoryCache {
                coil.memory.MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .allowRgb565(true)
            .crossfade(true)
            .build()

    // Jedna instancja na cały cykl życia appki — MainActivity/AppViewModel korzystają z
    // TEJ SAMEJ instancji (przez applicationContext), żeby stan zakupu/próby był spójny
    // niezależnie od tego, ile razy MainActivity zostanie stworzone na nowo (np. przy
    // obrocie ekranu). Patrz obszerne zastrzeżenie na górze BillingManager.kt.
    val billingManager: BillingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()
        CrashLogger.install(this)
        // Data pierwszego uruchomienia — zapisywana RAZ, nigdy nadpisywana. Patrz
        // AppViewModel.isTrialExpired po pełne wyjaśnienie mechanizmu okresu próbnego.
        // Zapis czysto LOKALNY (SharedPreferences) — żadnego połączenia sieciowego.
        com.iptvplayer.app.data.LocalStore(this).setFirstRunTimestampIfAbsent()
        // BRAMKA AKCEPTACJI (sekcja 2 specyfikacji: "Bez akceptacji aplikacja nie działa"):
        // dopóki Użytkownik nie zaznaczy pola i nie naciśnie przycisku akceptacji na ekranie
        // powitalnym, appka NIE wykonuje żadnych połączeń sieciowych — ani rozmowy z Google
        // Play (Billing), ani odświeżania katalogu w tle (WorkManager), ani nasłuchu
        // rzutowania z telefonu (gniazdo sieciowe na TV). Wszystko to startuje dopiero z
        // startNetworkServices(), wołanego (a) tu, jeśli akceptacja już była wcześniej, albo
        // (b) z AppViewModel.onOnboardingAccepted() w chwili akceptacji.
        if (com.iptvplayer.app.data.LocalStore(this).hasAcceptedOnboarding()) {
            startNetworkServices()
        }
    }

    private var networkServicesStarted = false

    /**
     * Uruchamia WSZYSTKO, co łączy się z siecią lub nasłuchuje w sieci. Bezpieczne do
     * wołania wielokrotnie (drugie i kolejne wywołania nic nie robią). Wołać WYŁĄCZNIE gdy
     * Użytkownik zaakceptował ekran powitalny — patrz komentarz w onCreate().
     */
    fun startNetworkServices() {
        if (networkServicesStarted) return
        networkServicesStarted = true
        billingManager.start()
        // Nocne/okresowe odświeżanie katalogu w tle — patrz CatalogRefreshWorker.
        com.iptvplayer.app.data.CatalogRefreshWorker.schedule(this)
        // Odbiornik rzutowania z telefonu — TYLKO na TV (na telefonie appka jest STRONĄ
        // WYSYŁAJĄCĄ, patrz przycisk "Wyślij do TV" w odtwarzaczu, nie ma sensu, żeby
        // telefon RÓWNIEŻ nasłuchiwał na przychodzące rzuty). Patrz obszerny komentarz w
        // CastManager.kt po szczegóły protokołu.
        if (isTvDevice()) {
            CastManager.startTvReceiver(this) { payload ->
                // 1.20: nic nie startuje bez potwierdzenia — najpierw okno na TV.
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    val intent = Intent(this, com.iptvplayer.app.cast.CastConfirmActivity::class.java).apply {
                        putExtra(com.iptvplayer.app.cast.CastConfirmActivity.EXTRA_URL, payload.streamUrl)
                        putExtra(com.iptvplayer.app.cast.CastConfirmActivity.EXTRA_TITLE, payload.title)
                        putExtra(com.iptvplayer.app.cast.CastConfirmActivity.EXTRA_UA, payload.userAgent)
                        putExtra(com.iptvplayer.app.cast.CastConfirmActivity.EXTRA_REFERER, payload.referer)
                        putExtra(com.iptvplayer.app.cast.CastConfirmActivity.EXTRA_POS, payload.positionMs)
                        putExtra(com.iptvplayer.app.cast.CastConfirmActivity.EXTRA_DEVICE, payload.deviceName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }
            }
        }
    }

    /** Uruchamia odtwarzanie rzutu — wołane WYŁĄCZNIE po kliknięciu [Odtwórz] w oknie
     * potwierdzenia (CastConfirmActivity). Wołane na głównym wątku. */
    fun launchCastPlayback(payload: CastManager.CastPayload) {
        val id = "cast_${System.currentTimeMillis()}"
        if (payload.positionMs > 0) {
            // Zapisane jako zwykły postęp oglądania pod TYM SAMYM id, które zaraz
            // trafi do kolejki — PlayerActivity i tak sprawdza zapisany postęp przy
            // starcie (patrz LaunchedEffect(current.id) w PlayerActivity.kt), więc
            // nie trzeba duplikować logiki wznowienia osobno dla rzutów.
            LocalStore(this).saveWatchProgress(
                id,
                WatchProgress(
                    channelOrEpisodeId = id,
                    positionMillis = payload.positionMs,
                    durationMillis = 0L,
                    updatedAt = System.currentTimeMillis(),
                    title = payload.title,
                    kind = MediaKind.MOVIE,
                    streamUrl = payload.streamUrl,
                    userAgent = payload.userAgent,
                    referer = payload.referer,
                ),
            )
        }
        val queue = NavQueue(
            kind = QueueKind.SIMILAR_MOVIES,
            items = listOf(
                QueueEntry(
                    id = id,
                    kind = MediaKind.MOVIE,
                    title = payload.title,
                    streamUrl = payload.streamUrl,
                    posterUrl = null,
                    userAgent = payload.userAgent,
                    referer = payload.referer,
                ),
            ),
            startIndex = 0,
        )
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_QUEUE, queue)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}
