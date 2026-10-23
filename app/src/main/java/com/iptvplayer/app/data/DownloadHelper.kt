package com.iptvplayer.app.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.iptvplayer.app.i18n.trFor

/**
 * Pobieranie plików filmów/seriali — odpowiednik downloadItem()/triggerBlobDownload()
 * z app.js. Web musiał obchodzić ograniczenia przeglądarki (fetch+blob, fallback <a download>).
 * Na Androidzie mamy do tego wbudowany system: DownloadManager sam obsługuje powiadomienie
 * z postępem, wznawianie po utracie sieci i zapis do folderu Pobrane — bez żadnych obejść.
 *
 * 1.22: pliki zapisują się w katalogu aplikacji i odtwarza je tylko VivaPilot.
 *
 * Tak jak w oryginale: dotyczy tylko VOD (filmy/seriale), nie strumieni Live (te nie są
 * pojedynczym plikiem do zapisania, tylko ciągłą transmisją).
 */
object DownloadHelper {

    fun downloadItem(
        context: Context,
        streamUrl: String,
        title: String,
        userAgent: String?,
        referer: String?,
        // Przy pobieraniu wielu odcinków naraz (cały sezon) pojedynczy toast na KAŻDY
        // odcinek byłby zalewem powiadomień — silent=true pomija go, a wywołujący
        // (MainActivity.downloadSeriesSeason) pokazuje jeden zbiorczy toast na koniec.
        silent: Boolean = false,
        // Język Toastów (patrz i18n/Strings.kt) — domyślnie "pl", żeby wywołania sprzed
        // dodania i18n (np. PlayerActivity, które jeszcze nie przekazuje tego argumentu)
        // dalej kompilowały się bez zmian.
        lang: String = "pl",
        // Plakat do wyświetlenia na ekranie "Pobrane" (DownloadsScreen) — opcjonalny,
        // domyślnie null dla wywołań sprzed dodania tego ekranu, które go jeszcze nie
        // przekazują (kompilują się dalej bez zmian).
        posterUrl: String? = null,
    ) {
        // 1.23: w wydaniach sklepowych pobierania nie ma (DOWNLOADS_ENABLED = false).
        if (!com.iptvplayer.app.BuildConfig.DOWNLOADS_ENABLED) return
        try {
            val safeName = title
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .trim()
                .ifBlank { "pobrany_plik" }
            val ext = streamUrl.substringAfterLast('.', "mp4").substringBefore('?').take(4)
            val fileName = "$safeName.$ext"

            val request = DownloadManager.Request(Uri.parse(streamUrl))
                .setTitle(title)
                .setDescription("VivaPilot • pobieranie")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // 1.22: katalog aplikacji (Android/data/<pakiet>/files/Download), nie publiczny
                // folder "Pobrane" — nie wymaga uprawnień, plik znika po odinstalowaniu appki
                // i nie jest widoczny w galerii ani w innych aplikacjach.
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            userAgent?.let { request.addRequestHeader("User-Agent", it) }
            referer?.let { request.addRequestHeader("Referer", it) }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val managerId = manager.enqueue(request)

            // DownloadManager sam nie daje żadnego sposobu na wylistowanie "moich pobrań z
            // tej appki" — trzeba samemu zapamiętać, co zleciliśmy, żeby ekran "Pobrane"
            // (Ustawienia -> Pobrane) miał co pokazać zamiast gołych numerów ID.
            LocalStore(context).addDownload(
                DownloadRecord(
                    managerId = managerId,
                    title = title,
                    posterUrl = posterUrl,
                    streamUrl = streamUrl,
                    userAgent = userAgent,
                    referer = referer,
                    inAppDir = true,
                ),
            )

            if (!silent) {
                Toast.makeText(context, trFor(lang, "download.startedToast", fileName), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, trFor(lang, "download.failedToast", e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }
}
