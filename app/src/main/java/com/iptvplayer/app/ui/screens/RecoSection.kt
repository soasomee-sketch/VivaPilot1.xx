package com.iptvplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iptvplayer.app.data.Channel
import com.iptvplayer.app.data.ListItemRef
import com.iptvplayer.app.data.MediaKind
import com.iptvplayer.app.data.SeriesInfo
import com.iptvplayer.app.i18n.tr
import com.iptvplayer.app.ui.theme.*
import com.iptvplayer.app.ui.tv.TvDialogButton
import com.iptvplayer.app.ui.tv.notDpadFocusable
import com.iptvplayer.app.ui.tv.tvFocusFrame
import com.iptvplayer.app.util.searchNormalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * "Propozycje do obejrzenia" na ekranie głównym — odpowiednik mechanizmu znanego z wersji na
 * Samsung Tizen: wiersz plakatów dobieranych według KONTEKSTÓW, które użytkownik zaznacza
 * checkboxami i może dowolnie łączyć (np. Akcja + Komedia, Lata 90 + Lata 80), plus dwa
 * szybkie tryby jednokrotnego wyboru: "Losowe" i "Ulubione".
 *
 * Skąd appka wie, jaki to gatunek: tak samo jak w wyszukiwaniu nastrojowym (GlobalSearch.kt) —
 * z NAZWY KATEGORII, którą playlista i tak już ma (np. "|PL| FILMY AKCJI"), bo to jedyne dane o
 * gatunku dostępne dla CAŁEGO katalogu bez osobnego zapytania na każdą pozycję. Rok — z
 * tytułu (yearFromTitle z GridScreens.kt), z tego samego powodu. Oceny celowo nie ma jako
 * kontekstu: Xtream nie podaje jej w wykazie, tylko w osobnym zapytaniu na każdy film.
 *
 * Reguła łączenia (jak w Tizen): w obrębie jednej grupy ("Gatunki" albo "Lata produkcji")
 * wynik to SUMA ("lub"), a między grupami — CZĘŚĆ WSPÓLNA ("i"). Wybór jest zapamiętywany
 * (AppUiState.recoMode / LocalStore.getRecoMode) w formacie "random", "favorites" albo
 * "ctx:id1,id2,...".
 */

/** Gatunek rozpoznawany po fragmentach nazwy kategorii (porównywane po złożeniu akcentów). */
internal class RecoGenre(val id: String, val labelKey: String, keywords: List<String>) {
    val keywords: List<String> = keywords.map { it.searchNormalize() }
}

/** Okres produkcji — zakres lat włącznie. */
internal class RecoPeriod(val id: String, val labelKey: String, val years: IntRange)

internal val RECO_GENRES = listOf(
    RecoGenre("action", "reco.ctx.action", listOf("akcj", "action", "aktion", "acción", "боевик", "бойовик", "экшн", "екшн")),
    RecoGenre("comedy", "reco.ctx.comedy", listOf("komedi", "comedy", "komödie", "comedia", "comédie", "комед")),
    RecoGenre("horror", "reco.ctx.horror", listOf("horror", "straszn", "grusel", "terror", "horreur", "ужас", "жах")),
    RecoGenre("romance", "reco.ctx.romance", listOf("roman", "liebes", "мелодрам", "романт")),
    RecoGenre("thriller", "reco.ctx.thriller", listOf("sensacj", "thriller", "krymina", "crime", "krimi", "polar", "триллер", "трилер", "детектив", "криминал", "кримінал")),
    RecoGenre("animation", "reco.ctx.animation", listOf("animowan", "animation", "animaci", "bajk", "kreskówk", "dzieci", "kids", "kinder", "infantil", "enfant", "мульт", "дитяч", "детск")),
    RecoGenre("documentary", "reco.ctx.documentary", listOf("dokument", "documental", "documentary", "documentaire", "документ")),
    RecoGenre("scifi", "reco.ctx.scifi", listOf("fantasy", "fantastyk", "sci-fi", "scifi", "science fiction", "fantasía", "фантаст", "фэнтези", "фентез")),
    RecoGenre("war", "reco.ctx.war", listOf("wojenn", "wojn", "krieg", "guerra", "guerre", "военн", "воєнн")),
    RecoGenre("family", "reco.ctx.family", listOf("familijn", "rodzinn", "family", "familie", "familia", "famille", "семейн", "сімейн")),
    RecoGenre("adventure", "reco.ctx.adventure", listOf("przygod", "adventure", "abenteuer", "aventura", "aventure", "приключ", "пригод")),
    RecoGenre("music", "reco.ctx.music", listOf("muzyczn", "musical", "music", "musik", "música", "musique", "музык", "мюзикл")),
)

internal val RECO_PERIODS = listOf(
    RecoPeriod("y2020", "reco.year.2020", 2020..2100),
    RecoPeriod("y2010", "reco.year.2010", 2010..2019),
    RecoPeriod("y2000", "reco.year.2000", 2000..2009),
    RecoPeriod("y1990", "reco.year.1990", 1990..1999),
    RecoPeriod("y1980", "reco.year.1980", 1980..1989),
    RecoPeriod("yOld", "reco.year.old", 1900..1979),
)

/** Tryb propozycji — patrz opis formatu zapisu wyżej. */
internal sealed class RecoMode {
    object Random : RecoMode()
    object Favorites : RecoMode()
    data class Contexts(val ids: Set<String>) : RecoMode()

    fun encode(): String = when (this) {
        Random -> "random"
        Favorites -> "favorites"
        is Contexts -> if (ids.isEmpty()) "random" else "ctx:" + ids.sorted().joinToString(",")
    }

    companion object {
        fun parse(raw: String?): RecoMode = when {
            raw == "favorites" -> Favorites
            raw != null && raw.startsWith("ctx:") -> {
                val known = (RECO_GENRES.map { it.id } + RECO_PERIODS.map { it.id }).toSet()
                val ids = raw.removePrefix("ctx:").split(",").map { it.trim() }.filter { it in known }.toSet()
                if (ids.isEmpty()) Random else Contexts(ids)
            }
            else -> Random
        }
    }
}

/** Jedna propozycja na półce — film albo serial (Live nie jest proponowane). */
internal sealed class RecoItem {
    abstract val id: String
    abstract val title: String
    abstract val group: String?
    abstract val posterUrl: String?

    class Movie(val channel: Channel) : RecoItem() {
        override val id get() = channel.id
        override val title get() = channel.name
        override val group get() = channel.group
        override val posterUrl get() = channel.logoUrl
    }

    class Series(val series: SeriesInfo) : RecoItem() {
        override val id get() = series.id
        override val title get() = series.name
        override val group get() = series.group
        override val posterUrl get() = series.posterUrl
    }
}

/** Ile plakatów maksymalnie na półce. */
private const val RECO_LIMIT = 24

/**
 * Wybiera propozycje według trybu. Czysta funkcja bez dostępu do UI — wołana na wątku w tle
 * (patrz RecoSection), bo przy katalogu liczącym dziesiątki tysięcy pozycji filtrowanie po
 * nazwach kategorii nie powinno blokować rysowania ekranu.
 */
internal fun computeRecoPicks(
    movies: List<Channel>,
    series: List<SeriesInfo>,
    mode: RecoMode,
    favorites: Set<String>,
    seed: Long,
): List<RecoItem> {
    val all = ArrayList<RecoItem>(movies.size + series.size)
    movies.forEach { all.add(RecoItem.Movie(it)) }
    series.forEach { all.add(RecoItem.Series(it)) }

    val pool: List<RecoItem> = when (mode) {
        RecoMode.Random -> all
        RecoMode.Favorites -> {
            // Inne pozycje z tych samych kategorii, w których użytkownik ma już ulubione.
            val favGroups = all.filter { it.id in favorites }.mapNotNull { it.group }.toSet()
            if (favGroups.isEmpty()) emptyList()
            else all.filter { it.id !in favorites && it.group != null && it.group in favGroups }
        }
        is RecoMode.Contexts -> {
            val genres = RECO_GENRES.filter { it.id in mode.ids }
            val periods = RECO_PERIODS.filter { it.id in mode.ids }
            // Nazw kategorii jest zwykle kilkaset, a pozycji dziesiątki tysięcy — składamy
            // akcenty raz na kategorię, nie raz na pozycję.
            val foldedGroups = HashMap<String, String>()
            all.filter { item ->
                val genreOk = genres.isEmpty() || run {
                    val g = item.group
                    if (g == null) false else {
                        val folded = foldedGroups.getOrPut(g) { g.searchNormalize() }
                        genres.any { genre -> genre.keywords.any { kw -> folded.contains(kw) } }
                    }
                }
                genreOk && (periods.isEmpty() || run {
                    val y = yearFromTitle(item.title)
                    y != null && y in 1900..2100 && periods.any { p -> y in p.years }
                })
            }
        }
    }
    if (pool.isEmpty()) return emptyList()
    return pool.shuffled(java.util.Random(seed)).take(RECO_LIMIT)
}

@Composable
private fun recoModeLabel(mode: RecoMode): String = when (mode) {
    RecoMode.Random -> "🎲 " + tr("reco.mode.random")
    RecoMode.Favorites -> "🎯 " + tr("reco.mode.favorites")
    is RecoMode.Contexts -> {
        val genres = RECO_GENRES.filter { it.id in mode.ids }.map { tr(it.labelKey) }
        val periods = RECO_PERIODS.filter { it.id in mode.ids }.map { tr(it.labelKey) }
        listOf(genres.joinToString(" + "), periods.joinToString(" + "))
            .filter { it.isNotEmpty() }
            .joinToString("  ·  ")
    }
}

/**
 * Półka "Propozycje do obejrzenia" na Home. Nic nie rysuje, gdy katalog nie ma filmów ani
 * seriali. Nagłówek zwija/rozwija się tak samo jak sekcje "Ostatnio dodane" (stan zapamiętany
 * pod kluczem "reco" w collapsedHomeSections).
 */
@Composable
internal fun RecoSection(
    movies: List<Channel>,
    series: List<SeriesInfo>,
    modeRaw: String,
    onModeChange: (String) -> Unit,
    favorites: Set<String>,
    collapsed: Boolean,
    onToggleCollapsed: () -> Unit,
    onOpenMovie: (Channel) -> Unit,
    onOpenSeries: (SeriesInfo) -> Unit,
    onQuickPlayMovie: (Channel) -> Unit,
    onQuickPlaySeries: (SeriesInfo) -> (() -> Unit)?,
    onToggleFavorite: (String) -> Unit,
    onDownloadMovie: (Channel) -> Unit,
    onAddToList: (ListItemRef) -> Unit,
    // true = okres próbny minął i appka nie jest kupiona — funkcja jest ZABLOKOWANA (jak
    // wyszukiwanie, Moje listy i pobieranie, patrz TrialGate.kt): zamiast plakatów jest tylko
    // wiersz z kłódką, a dotknięcie go pokazuje okno zakupu (onLockedClick).
    locked: Boolean = false,
    onLockedClick: () -> Unit = {},
) {
    if (movies.isEmpty() && series.isEmpty()) return
    val mode = remember(modeRaw) { RecoMode.parse(modeRaw) }
    var seed by remember { mutableStateOf(System.nanoTime()) }
    var showPicker by remember { mutableStateOf(false) }
    var picks by remember { mutableStateOf<List<RecoItem>?>(null) }
    // Ulubione czytane przez rememberUpdatedState, a NIE jako klucz efektu niżej — dzięki temu
    // dodanie/odjęcie gwiazdki na plakacie nie losuje całej półki od nowa pod palcem
    // użytkownika (przeliczenie następuje przy zmianie trybu, katalogu albo "Losuj ponownie").
    val favoritesNow by rememberUpdatedState(favorites)
    LaunchedEffect(movies, series, mode, seed, locked) {
        // Zablokowane = nic nie liczymy (funkcja nie jest dostępna), poprzednie wyniki są zbędne.
        if (locked) { picks = null; return@LaunchedEffect }
        val favSnapshot = favoritesNow
        picks = withContext(Dispatchers.Default) {
            computeRecoPicks(movies, series, mode, favSnapshot, seed)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusFrame(shape = RoundedCornerShape(6.dp))
            .clickable(onClick = onToggleCollapsed)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(tr("reco.title"), color = TextDim, style = MaterialTheme.typography.labelSmall)
        Text(if (collapsed) "▾" else "▴", color = TextDim, style = MaterialTheme.typography.labelSmall)
    }
    if (collapsed) {
        Spacer(Modifier.height(8.dp))
        return
    }
    Spacer(Modifier.height(8.dp))

    if (locked) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .tvFocusFrame(shape = RoundedCornerShape(12.dp))
                .clickable(onClick = onLockedClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("\uD83D\uDD12", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(10.dp))
            Text(
                tr("reco.locked"),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(20.dp))
        return
    }

    // --- pasek: aktualny wybór (dotknięcie otwiera okno wyboru) + losowanie od nowa ---
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            recoModeLabel(mode),
            color = Accent,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Surface)
                .tvFocusFrame(shape = RoundedCornerShape(18.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Surface)
                .tvFocusFrame(shape = CircleShape)
                .clickable { seed = System.nanoTime() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Casino,
                contentDescription = tr("reco.rerollDesc"),
                tint = TextMain,
                modifier = Modifier.size(20.dp),
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    val current = picks
    if (current != null && current.isEmpty()) {
        Text(
            tr(if (mode == RecoMode.Favorites) "reco.empty.favorites" else "reco.empty.ctx"),
            color = TextDim,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    } else if (current != null) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Bez `key` — id kanału/serialu nie musi być unikalne między różnymi playlistami, a
            // powtórzony klucz w LazyRow kończy się wyjątkiem (tak samo robi RecentlyAddedSection).
            items(current) { item ->
                when (item) {
                    is RecoItem.Movie -> RecentlyAddedTile(
                        title = item.title,
                        posterUrl = item.posterUrl,
                        onClick = { onOpenMovie(item.channel) },
                        isFavorite = item.id in favorites,
                        onToggleFavorite = { onToggleFavorite(item.id) },
                        onDownload = { onDownloadMovie(item.channel) },
                        onAddToList = { onAddToList(ListItemRef(id = item.id, kind = MediaKind.MOVIE)) },
                        onQuickPlay = { onQuickPlayMovie(item.channel) },
                    )
                    is RecoItem.Series -> RecentlyAddedTile(
                        title = item.title,
                        posterUrl = item.posterUrl,
                        onClick = { onOpenSeries(item.series) },
                        isFavorite = item.id in favorites,
                        onToggleFavorite = { onToggleFavorite(item.id) },
                        onAddToList = { onAddToList(ListItemRef(id = item.id, kind = MediaKind.SERIES)) },
                        onQuickPlay = onQuickPlaySeries(item.series),
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))

    if (showPicker) {
        RecoContextPicker(
            current = mode,
            onApply = { newMode ->
                showPicker = false
                onModeChange(newMode.encode())
                // Nowy wybór = świeże losowanie, także gdy zastosowano ten sam zestaw.
                seed = System.nanoTime()
            },
            onDismiss = { showPicker = false },
        )
    }
}

/**
 * Okno wyboru kontekstów: na górze dwa szybkie tryby jednokrotnego wyboru ("Losowe",
 * "Ulubione" — od razu stosują się i zamykają okno), niżej dwie grupy checkboxów (gatunki,
 * lata produkcji), które można dowolnie łączyć; "Zastosuj" zatwierdza zaznaczenie.
 */
@Composable
private fun RecoContextPicker(
    current: RecoMode,
    onApply: (RecoMode) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember {
        mutableStateOf((current as? RecoMode.Contexts)?.ids ?: emptySet())
    }
    fun toggle(id: String) {
        selected = if (id in selected) selected - id else selected + id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("reco.dialog.title")) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                item { RecoPickerHeader(tr("reco.dialog.modeHeader")) }
                item {
                    RecoPickerRow(
                        label = "🎲 " + tr("reco.mode.random"),
                        checked = null,
                        onClick = { onApply(RecoMode.Random) },
                    )
                }
                item {
                    RecoPickerRow(
                        label = "🎯 " + tr("reco.mode.favorites"),
                        checked = null,
                        onClick = { onApply(RecoMode.Favorites) },
                    )
                }
                item { RecoPickerHeader(tr("reco.dialog.genresHeader")) }
                items(RECO_GENRES, key = { it.id }) { genre ->
                    RecoPickerRow(
                        label = tr(genre.labelKey),
                        checked = genre.id in selected,
                        onClick = { toggle(genre.id) },
                    )
                }
                item { RecoPickerHeader(tr("reco.dialog.yearsHeader")) }
                items(RECO_PERIODS, key = { it.id }) { period ->
                    RecoPickerRow(
                        label = tr(period.labelKey),
                        checked = period.id in selected,
                        onClick = { toggle(period.id) },
                    )
                }
                item {
                    Text(
                        tr("reco.dialog.hint"),
                        color = TextDim,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TvDialogButton(
                text = tr("reco.dialog.apply", selected.size),
                onClick = { onApply(RecoMode.Contexts(selected)) },
            )
        },
        dismissButton = {
            Row {
                TvDialogButton(text = tr("reco.dialog.clear"), onClick = { selected = emptySet() })
                TvDialogButton(text = tr("common.cancel"), onClick = onDismiss)
            }
        },
    )
}

@Composable
private fun RecoPickerHeader(text: String) {
    Text(
        text,
        color = TextDim,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp),
    )
}

/** Wiersz okna wyboru; `checked == null` oznacza wiersz-akcję bez checkboxa. */
@Composable
private fun RecoPickerRow(label: String, checked: Boolean?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusFrame(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = TextMain,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (checked != null) {
            // Cały wiersz jest celem fokusu/dotyku — sam checkbox tylko pokazuje stan.
            Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.notDpadFocusable())
        }
    }
}
