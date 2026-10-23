package com.iptvplayer.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iptvplayer.app.i18n.LocalLang
import com.iptvplayer.app.i18n.tr
import com.iptvplayer.app.i18n.trFor
import com.iptvplayer.app.ui.AppUiState
import com.iptvplayer.app.ui.theme.*
import com.iptvplayer.app.ui.tv.tvFocusFrame
import java.text.SimpleDateFormat
import java.util.*

/** Czytelny opis "kiedy ostatnio" dla znacznika lastRefreshAt spod przycisku Odśwież na Home —
 * 0L oznacza, że w tej instalacji jeszcze nigdy się nie udało (albo nie próbowano). `lang`
 * podawany jawnie (zamiast LocalLang.current wewnątrz) — to zwykła funkcja, nie @Composable,
 * więc czyta go wywołujący (patrz użycie niżej) i przekazuje też do SimpleDateFormat, żeby
 * nazwy dni/miesięcy też były w wybranym języku, nie w domyślnym języku urządzenia. */
internal fun formatLastRefresh(timestampMs: Long, lang: String): String {
    if (timestampMs <= 0L) return trFor(lang, "home.lastRefresh.never")
    val diffMs = System.currentTimeMillis() - timestampMs
    val diffMin = diffMs / 60_000
    val locale = Locale(lang)
    return when {
        diffMin < 1 -> trFor(lang, "home.lastRefresh.justNow")
        diffMin < 60 -> trFor(lang, "home.lastRefresh.minutesAgo", diffMin)
        else -> {
            val sameDay = SimpleDateFormat("yyyyMMdd", locale).format(Date(timestampMs)) ==
                SimpleDateFormat("yyyyMMdd", locale).format(Date())
            if (sameDay) {
                SimpleDateFormat("HH:mm", locale).format(Date(timestampMs))
            } else {
                SimpleDateFormat("d MMM, HH:mm", locale).format(Date(timestampMs))
            }
        }
    }
}

/**
 * Karta statusu biblioteki (liczba playlist/kanałów/filmów/seriali, czas ostatniego
 * odświeżenia, przycisk Odśwież). Do wersji 1.14 stała pod zegarem na Home, od 1.15
 * mieszka w Ustawieniach.
 */
@Composable
fun LibraryStatusCard(
    state: AppUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalLang.current
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (state.playlists.isEmpty()) tr("home.status.noPlaylist")
                else tr("home.status.summary", state.playlists.size, state.liveChannels.size),
                color = TextMain, style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr("home.status.moviesSeries", state.movies.size, state.series.size),
                color = TextDim, style = MaterialTheme.typography.bodySmall,
            )
            Text(
                if (state.isLoading) tr("home.status.refreshing")
                else tr("home.status.lastRefreshed", formatLastRefresh(state.lastRefreshAt, lang)),
                color = TextDim, style = MaterialTheme.typography.labelSmall,
            )
        }
        // Ikona kręci się, dopóki isLoading == true; w trakcie odświeżania przycisk jest
        // zablokowany, żeby nie odpalać kilku równoległych zapytań.
        val infiniteTransition = rememberInfiniteTransition(label = "refresh-spin")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "refresh-spin-angle",
        )
        IconButton(
            onClick = {
                if (state.playlists.isEmpty()) {
                    Toast.makeText(context, trFor(lang, "home.status.refreshToast"), Toast.LENGTH_SHORT).show()
                } else {
                    onRefresh()
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.tvFocusFrame(shape = RoundedCornerShape(10.dp)),
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = tr("home.status.refreshDesc"),
                tint = TextDim,
                modifier = if (state.isLoading) Modifier.rotate(rotation) else Modifier,
            )
        }
    }
}
