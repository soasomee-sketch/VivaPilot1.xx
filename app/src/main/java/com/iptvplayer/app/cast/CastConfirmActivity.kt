package com.iptvplayer.app.cast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.iptvplayer.app.IptvApp
import com.iptvplayer.app.data.LocalStore
import com.iptvplayer.app.i18n.LocalLang
import com.iptvplayer.app.i18n.tr
import com.iptvplayer.app.ui.theme.IptvPlayerTheme
import com.iptvplayer.app.ui.tv.TvDialogButton

/**
 * Okno potwierdzenia na TV (1.20, punkt 4): "Urządzenie [nazwa] chce odtworzyć [tytuł].
 * [Odtwórz] · [Odrzuć]". Obsługa pilotem (TvDialogButton, fokus startowy na [Odrzuć], żeby
 * przypadkowe OK niczego nie uruchomiło). Bez kliknięcia [Odtwórz] odtwarzanie się NIE
 * rozpoczyna; Wstecz = odrzucenie.
 */
class CastConfirmActivity : ComponentActivity() {

    private var payload by mutableStateOf<CastManager.CastPayload?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payload = readPayload(intent)
        val lang = LocalStore(this).getLanguage()
        setContent {
            IptvPlayerTheme {
                CompositionLocalProvider(LocalLang provides lang) {
                    val p = payload
                    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize())
                    if (p != null) {
                        BackHandler { finish() }
                        val device = p.deviceName.ifBlank { tr("cast.confirm.unknownDevice") }
                        AlertDialog(
                            onDismissRequest = { finish() },
                            title = { Text(tr("cast.confirm.title")) },
                            text = { Text(tr("cast.confirm.text", device, p.title)) },
                            confirmButton = {
                                TvDialogButton(
                                    text = tr("cast.confirm.play"),
                                    onClick = {
                                        (application as IptvApp).launchCastPlayback(p)
                                        finish()
                                    },
                                )
                            },
                            dismissButton = {
                                TvDialogButton(
                                    text = tr("cast.confirm.reject"),
                                    initialFocus = true,
                                    onClick = { finish() },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        payload = readPayload(intent)
    }

    private fun readPayload(i: android.content.Intent): CastManager.CastPayload? {
        val url = i.getStringExtra(EXTRA_URL) ?: return null
        // Druga linia obrony: adres sprawdzony także tutaj, zanim cokolwiek się uruchomi.
        if (!CastManager.isAllowedStreamUrl(url)) return null
        return CastManager.CastPayload(
            streamUrl = url,
            title = i.getStringExtra(EXTRA_TITLE).orEmpty(),
            userAgent = i.getStringExtra(EXTRA_UA),
            referer = i.getStringExtra(EXTRA_REFERER),
            positionMs = i.getLongExtra(EXTRA_POS, 0L),
            deviceName = i.getStringExtra(EXTRA_DEVICE).orEmpty(),
        )
    }

    companion object {
        const val EXTRA_URL = "cast_url"
        const val EXTRA_TITLE = "cast_title"
        const val EXTRA_UA = "cast_ua"
        const val EXTRA_REFERER = "cast_referer"
        const val EXTRA_POS = "cast_pos"
        const val EXTRA_DEVICE = "cast_device"
    }
}
