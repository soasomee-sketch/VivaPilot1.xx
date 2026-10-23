package com.iptvplayer.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iptvplayer.app.data.PlaylistSource
import com.iptvplayer.app.data.PlaylistStatus
import com.iptvplayer.app.i18n.SUPPORTED_LANGUAGES
import com.iptvplayer.app.i18n.tr
import com.iptvplayer.app.i18n.trFor
import com.iptvplayer.app.ui.nav.Tab
import com.iptvplayer.app.ui.theme.*
import com.iptvplayer.app.ui.tv.tvFocusFrame
import com.iptvplayer.app.ui.tv.tvKeyboardOnDemand

private val languages = SUPPORTED_LANGUAGES

/** Sekcja menedżera kategorii — jeden typ (Live/Movies/Series). `labelKey` odsyła do tej
 * samej tabeli tłumaczeń co etykiety dolnej nawigacji (patrz Tab.labelKey), żeby "Live"/
 * "Movies"/"Series" tutaj i na dole ekranu zawsze się zgadzały. */
private data class CatSection(val type: String, val labelKey: String, val categories: List<String>)

/**
 * Odpowiednik zakładki Settings: wybór języka, menedżer kategorii (ukrywanie kategorii
 * per typ, tak jak renderCategoryManager()/toggleCategoryDisabled() w app.js) + info o aplikacji.
 */
@Composable
fun SettingsScreen(
    currentLang: String,
    onLangSelected: (String) -> Unit,
    autoCloseAddToList: Boolean = false,
    onAutoCloseAddToListChanged: (Boolean) -> Unit = {},
    // Karta statusu biblioteki (liczby, ostatnie odświeżenie, Odśwież) — przeniesiona z Home w 1.15.
    state: com.iptvplayer.app.ui.AppUiState,
    onRefresh: () -> Unit = {},
    // Cel fokusu przy zejściu pilotem W DÓŁ z dolnego paska nawigacji — ten sam mechanizm
    // co w LiveScreen.kt/HomeScreen.kt/GridScreens.kt (tam obszerniejszy komentarz z
    // uzasadnieniem). Tutaj podpięty pod logo na samej górze ekranu (patrz niżej) — już
    // zawsze obecne, nie trzeba dodawać osobnej niewidocznej kotwicy jak w LocalFolderScreen.
    topFocusRequester: androidx.compose.ui.focus.FocusRequester? = null,
    allCategoriesLive: List<String> = emptyList(),
    allCategoriesMovie: List<String> = emptyList(),
    allCategoriesSeries: List<String> = emptyList(),
    hiddenCategories: Map<String, Set<String>> = emptyMap(),
    onToggleCategory: (type: String, category: String) -> Unit = { _, _ -> },
    onSetAllCategoriesHidden: (type: String, categories: List<String>, hidden: Boolean) -> Unit = { _, _, _ -> },
    onApplyCategoryPrefilter: (prefix: String) -> Unit = {},
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    // --- Playlisty IPTV (M3U/Xtream) — dawniej osobna zakładka na dolnym pasku, teraz cała
    // funkcjonalność (dodawanie/edycja/usuwanie) mieszka tutaj, patrz onManagePlaylists. ---
    playlists: List<PlaylistSource> = emptyList(),
    playlistStatuses: Map<String, PlaylistStatus> = emptyMap(),
    onManagePlaylists: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    // Po okresie próbnym bez zakupu (od 1.16): Zarządzanie kategoriami i „Pobrane” są
    // wyszarzone; dotknięcie otwiera okno zakupu (onLockedClick). Dotychczasowe ustawienia
    // kategorii nadal działają, a pobrane pliki zostają na urządzeniu.
    settingsLocked: Boolean = false,
    onLockedClick: () -> Unit = {},
    // --- Informacje prawne (sekcja 7 specyfikacji zgodności prawnej z 09.10) — patrz nowa
    // sekcja na końcu tego ekranu. onOpenRegulamin/onOpenPolityka otwierają nakładkę
    // LegalDocumentScreen zarządzaną przez RootScreen (ten sam wzorzec co onOpenDownloads
    // wyżej); licencje open source otwierają się same, bezpośrednio stąd (zwykła Activity
    // dostarczona przez oss-licenses-plugin, nie nakładka Compose). ---
    onOpenRegulamin: () -> Unit = {},
    onOpenPolityka: () -> Unit = {},
    // "Moje zgody" — ekran tylko do odczytu z zapisanym rekordem akceptacji (sekcja 7).
    onOpenConsents: () -> Unit = {},
    isFullVersionPurchased: Boolean = false,
    onShowPurchaseConfirmation: () -> Unit = {},
    onRefreshPurchases: () -> Unit = {},
) {
    // Regulamin i Polityka prywatności są wbudowane w appkę (assets/legal, patrz
    // LegalDocuments.kt) i otwierane z sekcji "Informacje prawne" niżej. LegalDocumentRow
    // (poniżej) to prosty, współdzielony komponent wiersza z chevronem.

    val sections = remember(allCategoriesLive, allCategoriesMovie, allCategoriesSeries) {
        listOf(
            CatSection("live", Tab.LIVE.labelKey, allCategoriesLive),
            CatSection("movie", Tab.MOVIES.labelKey, allCategoriesMovie),
            CatSection("series", Tab.SERIES.labelKey, allCategoriesSeries),
        ).filter { it.categories.isNotEmpty() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(16.dp),
    ) {
        // --- Logo marki, na samej górze ekranu (patrz res/drawable/logo_vivapilot_wordmark.png) ---
        item {
            Image(
                painter = painterResource(com.iptvplayer.app.R.drawable.logo_vivapilot_wordmark),
                contentDescription = "VivaPilot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 24.dp)
                    .then(if (topFocusRequester != null) Modifier.focusRequester(topFocusRequester) else Modifier)
                    .focusable(),
            )
        }

        item {
            LibraryStatusCard(state = state, onRefresh = onRefresh)
            Spacer(Modifier.height(20.dp))
        }

        item {
            Text(tr("settings.playlists.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Text(
                tr("settings.playlists.desc"),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            PlaylistsSummaryRow(playlists = playlists, statuses = playlistStatuses, onClick = onManagePlaylists)
            Spacer(Modifier.height(20.dp))
        }

        item {
            Text(tr("settings.language.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
        }
        item {
            // Zwinięte domyślnie — pokazuje tylko aktualnie wybrany język jako JEDEN
            // przystanek D-pada, zamiast zawsze renderować wszystkie 7 opcji na raz
            // (zgłoszenie: trzeba było przechodzić pilotem przez każdy język osobno, żeby
            // dotrzeć do tego na końcu listy). Ten sam wzorzec zwijania co przy kategoriach
            // w Live/Filmy/Seriale.
            var showLanguageOptions by remember { mutableStateOf(false) }
            val currentLabel = languages.find { it.first == currentLang }?.second ?: currentLang
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .tvFocusFrame(shape = RoundedCornerShape(10.dp))
                    .clickable { showLanguageOptions = !showLanguageOptions }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(currentLabel, color = TextMain, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (showLanguageOptions) "\u25B4" else "\u25BE",
                    color = TextDim,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (showLanguageOptions) {
                Column {
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface)
                                .tvFocusFrame(shape = RoundedCornerShape(10.dp))
                                .clickable {
                                    onLangSelected(code)
                                    showLanguageOptions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = code == currentLang,
                                onClick = { onLangSelected(code); showLanguageOptions = false },
                            )
                            Text(label, color = TextMain, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (sections.isNotEmpty() && settingsLocked) {
            item {
                Spacer(Modifier.height(20.dp))
                Text(tr("settings.categories.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
                Text(
                    tr("settings.categories.desc"),
                    color = TextDim,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )
                LockedSettingsRow(onClick = onLockedClick)
            }
        } else if (sections.isNotEmpty()) {
            item {
                Spacer(Modifier.height(20.dp))
                Text(tr("settings.categories.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
                Text(
                    tr("settings.categories.desc"),
                    color = TextDim,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )
                // Wstępna preselekcja po prefiksie w nazwie kategorii (np. "PL" dla "PL|
                // FILMOWE") — JEDNORAZOWA akcja, nie stałe ustawienie: ukrywa od razu
                // wszystkie kategorie inne niż pasujące, dając gotowy punkt startowy zamiast
                // ręcznego przechodzenia przez dziesiątki kategorii od zera. Dalej można
                // doprecyzować pojedyncze kategorie tak jak zawsze, patrz lista niżej.
                var prefilterInput by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = prefilterInput,
                        onValueChange = { prefilterInput = it },
                        label = { Text(tr("settings.categories.prefilterLabel")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).tvKeyboardOnDemand(),
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { onApplyCategoryPrefilter(prefilterInput) },
                        modifier = Modifier.tvFocusFrame(shape = CircleShape),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = tr("common.save"), tint = Accent)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            items(sections) { sec ->
                CategoryManagerTile(
                    section = sec,
                    hidden = hiddenCategories[sec.type].orEmpty(),
                    onToggleCategory = { cat -> onToggleCategory(sec.type, cat) },
                    onSelectAll = { onSetAllCategoriesHidden(sec.type, sec.categories, false) },
                    onDeselectAll = { onSetAllCategoriesHidden(sec.type, sec.categories, true) },
                )
            }
        }

        if (com.iptvplayer.app.BuildConfig.DOWNLOADS_ENABLED) item {
            Spacer(Modifier.height(20.dp))
            Text(tr("settings.downloads.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            if (settingsLocked) {
                LockedSettingsRow(title = tr("downloads.title"), onClick = onLockedClick)
            } else {
                LegalDocumentRow(tr("downloads.title"), onClick = onOpenDownloads)
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(tr("settings.addToList.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tr("settings.addToList.autoCloseLabel"),
                    color = TextMain,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                Switch(
                    checked = autoCloseAddToList,
                    onCheckedChange = onAutoCloseAddToListChanged,
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = Bg),
                )
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(tr("settings.help.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            var showPhoneHelp by remember { mutableStateOf(false) }
            var showTvHelp by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .tvFocusFrame(shape = RoundedCornerShape(10.dp))
                    .clickable { showPhoneHelp = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tr("settings.help.phoneTitle"), color = TextMain, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDim)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .tvFocusFrame(shape = RoundedCornerShape(10.dp))
                    .clickable { showTvHelp = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tr("settings.help.tvTitle"), color = TextMain, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDim)
            }
            if (showPhoneHelp) {
                AlertDialog(
                    onDismissRequest = { showPhoneHelp = false },
                    title = { Text(tr("settings.help.phoneTitle")) },
                    text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text(tr("settings.help.phoneBody"), color = TextMain, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPhoneHelp = false }) { Text(tr("common.close")) }
                    },
                )
            }
            if (showTvHelp) {
                AlertDialog(
                    onDismissRequest = { showTvHelp = false },
                    title = { Text(tr("settings.help.tvTitle")) },
                    text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text(tr("settings.help.tvBody"), color = TextMain, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTvHelp = false }) { Text(tr("common.close")) }
                    },
                )
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(tr("settings.backup.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Text(
                tr("settings.backup.desc"),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CatBulkButton(tr("settings.backup.export"), onExportBackup, Modifier.weight(1f))
                CatBulkButton(tr("settings.backup.import"), onImportBackup, Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(tr("settings.about.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                tr("settings.about.body"),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        // --- Informacje prawne (sekcja 7 specyfikacji zgodności prawnej z 09.10) ---
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Spacer(Modifier.height(20.dp))
            Text(tr("settings.legal.header"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))

            LegalDocumentRow(tr("legal.regulaminTitle"), onClick = onOpenRegulamin)
            Spacer(Modifier.height(6.dp))
            LegalDocumentRow(tr("legal.politykaTitle"), onClick = onOpenPolityka)
            Spacer(Modifier.height(6.dp))
            // Zapisany rekord zgód — tylko podgląd, bez edycji (sekcja 7 specyfikacji).
            LegalDocumentRow(tr("settings.legal.consentsRow"), onClick = onOpenConsents)
            Spacer(Modifier.height(6.dp))
            // Strona internetowa z dokumentami prawnymi — wymóg Google Play (sekcja "App
            // content" w Play Console potrzebuje żywego linku). Adres to na razie placeholder
            // (patrz LEGAL_WEBSITE_URL w LegalDocuments.kt) — Damian uzupełni docelowy po
            // opublikowaniu strony, appka i tak działa poprawnie bez tego.
            LegalDocumentRow(
                tr("settings.legal.websiteRow"),
                onClick = {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(com.iptvplayer.app.data.LEGAL_WEBSITE_URL),
                            ),
                        )
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
            // Wymagany, wyeksponowany wpis o libVLC — patrz obszerny komentarz przy kluczu
            // settings.legal.libvlcNotice w Strings.kt o tym, dlaczego to osobny tekst, NAD
            // linkiem do automatycznej listy niżej, a nie tylko poleganie na tamtej liście.
            Text(tr("settings.legal.libvlcNotice"), color = TextDim, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            // Lista licencji open source — dostarczona automatycznie przez
            // com.google.android.gms.oss-licenses-plugin (patrz app/build.gradle.kts), z
            // wpisem o libVLC/LGPL-2.1 doklejanym na starcie tej Activity (patrz
            // OssLicensesMenuActivity.setActivityTitle niżej i obszerny komentarz w
            // app/build.gradle.kts o konfiguracji wtyczki).
            LegalDocumentRow(
                tr("settings.legal.licensesRow"),
                onClick = {
                    com.google.android.gms.oss.licenses.OssLicensesMenuActivity.setActivityTitle(
                        trFor(currentLang, "settings.legal.licensesRow"),
                    )
                    context.startActivity(
                        android.content.Intent(context, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java),
                    )
                },
            )

            Spacer(Modifier.height(16.dp))
            Text(tr("purchaseConfirmation.publisherRow"), color = TextDim, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(16.dp))
            Text(tr("settings.legal.purchaseHeader"), color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                tr(if (isFullVersionPurchased) "settings.legal.purchasedStatus" else "settings.legal.notPurchasedStatus"),
                color = TextMain,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isFullVersionPurchased) {
                    CatBulkButton(tr("settings.legal.showConfirmationButton"), onShowPurchaseConfirmation, Modifier.weight(1f))
                }
                CatBulkButton(tr("settings.legal.refreshPurchaseButton"), onRefreshPurchases, Modifier.weight(1f))
            }
            // Prywatność reklam — TYLKO w wydaniu z reklamami i tylko gdy reklamy są aktywne
            // (sekcja 7 i 9 specyfikacji). isPrivacyOptionsAvailable zwraca zawsze false w
            // wariancie `noads` (patrz com.iptvplayer.app.ads.AdsController), więc ten wiersz
            // sam znika w tym wydaniu bez żadnego dodatkowego `if (BuildConfig.FLAVOR...)`.
            if (com.iptvplayer.app.ads.AdsController.isPrivacyOptionsAvailable(context)) {
                Spacer(Modifier.height(6.dp))
                LegalDocumentRow(
                    tr("settings.legal.adsPrivacyRow"),
                    onClick = {
                        (context as? android.app.Activity)?.let {
                            com.iptvplayer.app.ads.AdsController.openPrivacyOptionsForm(it)
                        }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                tr("settings.legal.versionRow", com.iptvplayer.app.BuildConfig.VERSION_NAME),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Generyczny klikalny wiersz z etykietą i strzałką w prawo (Chevron sugeruje, że to
 * prowadzi dalej) — ten sam wzorzec wizualny co przy playliście w sekcji wyżej. Nazwa
 * historyczna (pierwotnie służył do wejścia w Regulamin/Politykę prywatności, patrz
 * komentarz wyżej dlaczego to zniknęło) — dziś używany wyłącznie dla wiersza "Pobrane". */
@Composable
private fun LegalDocumentRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .tvFocusFrame(shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextMain, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDim)
    }
}

/** Wyszarzony wiersz z kłódką — funkcja zablokowana po okresie próbnym bez zakupu
 * (patrz settingsLocked); dotknięcie otwiera okno zakupu. */
@Composable
private fun LockedSettingsRow(title: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .tvFocusFrame(shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            if (title != null) {
                Text(title, color = TextDim.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "\uD83D\uDD12 " + tr("settings.locked"),
                color = TextDim.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Skrót do pełnego ekranu zarządzania playlistami (patrz PlaylistsScreen, otwierany jako
 * nakładka z MainActivity) — pokazuje szybkie podsumowanie stanu, żeby nie trzeba było
 * wchodzić do środka tylko po to, by sprawdzić, czy wszystko działa. */
@Composable
private fun PlaylistsSummaryRow(
    playlists: List<PlaylistSource>,
    statuses: Map<String, PlaylistStatus>,
    onClick: () -> Unit,
) {
    val failing = playlists.count { statuses[it.id]?.ok == false }
    val summary = when {
        playlists.isEmpty() -> tr("settings.playlists.none")
        failing == 0 -> tr("settings.playlists.allOk", playlists.size)
        else -> tr("settings.playlists.someFailing", playlists.size, failing)
    }
    val dotColor = when {
        playlists.isEmpty() -> Danger
        failing == 0 -> Success
        failing == playlists.size -> Danger
        else -> Warning
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .tvFocusFrame(shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(tr("settings.playlists.manage"), color = TextMain, style = MaterialTheme.typography.bodyMedium)
            Text(summary, color = TextDim, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDim)
    }
}

@Composable
private fun CategoryManagerTile(
    section: CatSection,
    hidden: Set<String>,
    onToggleCategory: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val enabledCount = section.categories.count { it !in hidden }

    // Jawne wyznaczenie trasy fokusu pilota: nagłówek -> Ukryj wszystko -> Pokaż wszystko ->
    // pierwsza kategoria. Zgłoszony błąd: domyślne, GEOMETRYCZNE wyszukiwanie kierunkowe
    // Compose "przeskakiwało" nad rzędem tych dwóch przycisków przy strzałce w dół z
    // nagłówka, lądując od razu na liście kategorii pod spodem — prawdopodobnie dlatego,
    // że dwa wąskie przyciski obok siebie (po .weight(1f) każdy) dają Compose niejednoznaczny
    // kandydat względem tego, co jest niżej. Jawne wskazanie next-focus usuwa tę
    // niejednoznaczność zamiast liczyć na to, że domyślny algorytm trafi poprawnie.
    val headerFocusRequester = remember { FocusRequester() }
    val selectAllFocusRequester = remember { FocusRequester() }
    val deselectAllFocusRequester = remember { FocusRequester() }
    val firstCategoryFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(headerFocusRequester)
                .focusProperties { if (expanded) down = selectAllFocusRequester }
                .tvFocusFrame(shape = RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(tr(section.labelKey), color = TextMain, style = MaterialTheme.typography.bodyMedium)
                Text(
                    tr("settings.categories.active", enabledCount, section.categories.size),
                    color = TextDim,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(if (expanded) "\u25B4" else "\u25BE", color = TextDim, style = MaterialTheme.typography.bodyMedium)
        }

        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CatBulkButton(
                    tr("settings.categories.selectAll"),
                    onSelectAll,
                    Modifier
                        .weight(1f)
                        .focusRequester(selectAllFocusRequester)
                        .focusProperties {
                            up = headerFocusRequester
                            right = deselectAllFocusRequester
                            if (section.categories.isNotEmpty()) down = firstCategoryFocusRequester
                        },
                )
                CatBulkButton(
                    tr("settings.categories.deselectAll"),
                    onDeselectAll,
                    Modifier
                        .weight(1f)
                        .focusRequester(deselectAllFocusRequester)
                        .focusProperties {
                            up = headerFocusRequester
                            left = selectAllFocusRequester
                            if (section.categories.isNotEmpty()) down = firstCategoryFocusRequester
                        },
                )
            }
            Spacer(Modifier.height(6.dp))
            Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                section.categories.forEachIndexed { index, cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let { if (index == 0) it.focusRequester(firstCategoryFocusRequester).focusProperties { up = selectAllFocusRequester } else it }
                            .tvFocusFrame(shape = RoundedCornerShape(8.dp))
                            .clickable { onToggleCategory(cat) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            cat,
                            color = TextMain,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = cat !in hidden,
                            onCheckedChange = { onToggleCategory(cat) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = Bg),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CatBulkButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .tvFocusFrame(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = TextMain, style = MaterialTheme.typography.labelSmall)
    }
}
