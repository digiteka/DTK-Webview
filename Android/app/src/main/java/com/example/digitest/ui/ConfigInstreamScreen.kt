package com.example.digitest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.digitest.DEFAULT_IS_MDTK
import com.example.digitest.DEFAULT_IS_SRC
import com.example.digitest.DEFAULT_IS_URL_REFERRER
import com.example.digitest.DEFAULT_IS_ZONE
import com.example.digitest.InstreamPreferences
import com.example.digitest.InstreamSharedConfig
import com.example.digitest.NewplayerMode
import com.example.digitest.autoplayValueFor
import com.example.digitest.effectiveConsentString
import com.example.digitest.resolveNewplayer
import com.example.digitest.ui.theme.DigiBlue
import com.example.digitest.ui.theme.DigiCard
import com.example.digitest.ui.theme.DigiCardBorder
import com.example.digitest.ui.theme.DigiTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PLAY_MODES = listOf("ON_CLICK", "VISIBLE_AT_FIFTY_PERCENT", "AUTOPLAY")
private val PLAY_MODE_LABELS = listOf("Click To Play", "Scroll To Play", "Autoplay")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstreamConfigScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // ── Paramètres communs ──────────────────────────────────────────────
    val initShared = remember { InstreamPreferences.getSharedConfig(context) }
    var sharedMdtk by rememberSaveable { mutableStateOf(initShared.mdtk ?: "") }
    var sharedZone by rememberSaveable { mutableStateOf(initShared.zone ?: "") }
    var sharedSrc by rememberSaveable { mutableStateOf(initShared.src ?: "") }
    var sharedUrl by rememberSaveable { mutableStateOf(initShared.urlReferrer ?: "") }
    var sharedTagParam by rememberSaveable { mutableStateOf(initShared.tagParam ?: "") }
    var consentStringEnabled by rememberSaveable { mutableStateOf(initShared.consentStringEnabled) }

    val tcString = effectiveConsentString(consentStringEnabled)

    fun persistShared() {
        InstreamPreferences.saveSharedConfig(
            context, InstreamSharedConfig(
                mdtk = sharedMdtk.trim().ifEmpty { null },
                zone = sharedZone.trim().ifEmpty { null },
                src = sharedSrc.trim().ifEmpty { null },
                urlReferrer = sharedUrl.trim().ifEmpty { null },
                tagParam = sharedTagParam.trim().ifEmpty { null },
                consentStringEnabled = consentStringEnabled
            )
        )
    }

    val initSdk = remember { InstreamPreferences.getSdkConfig(context) }
    var sdkPlayMode by rememberSaveable { mutableStateOf(initSdk.playMode) }

    val initNoSdk = remember { InstreamPreferences.getNoSdkConfig(context) }
    var noSdkChromeless by rememberSaveable { mutableStateOf(initNoSdk.chromeless) }
    var noSdkCustomUrl by rememberSaveable { mutableStateOf(initNoSdk.customUrl ?: "") }
    var noSdkNewplayerMode by rememberSaveable { mutableStateOf(initNoSdk.newplayerMode ?: NewplayerMode.LEGACY.name) }
    var noSdkNewplayerBranchName by rememberSaveable { mutableStateOf(initNoSdk.newplayerBranchName ?: "") }
    var noSdkNewplayerLocalIP by rememberSaveable { mutableStateOf(initNoSdk.newplayerLocalIP ?: "") }

    fun persistNoSdk() {
        InstreamPreferences.saveNoSdkConfig(
            context,
            chromeless = noSdkChromeless,
            customUrl = noSdkCustomUrl.trim().ifEmpty { null },
            newplayerMode = noSdkNewplayerMode,
            newplayerBranchName = noSdkNewplayerBranchName.trim().ifEmpty { null },
            newplayerLocalIP = noSdkNewplayerLocalIP.trim().ifEmpty { null }
        )
    }

    var copiedUrl by remember { mutableStateOf(false) }

    val noSdkUrlPreview = noSdkCustomUrl.trim().ifEmpty {
        buildIframeUrl(
            tcString = tcString,
            mdtk = sharedMdtk.trim().ifEmpty { DEFAULT_IS_MDTK },
            zone = sharedZone.trim().ifEmpty { DEFAULT_IS_ZONE },
            src = sharedSrc.trim().ifEmpty { DEFAULT_IS_SRC },
            urlReferrer = sharedUrl.trim(),
            chromeless = noSdkChromeless,
            autoplay = autoplayValueFor(sdkPlayMode),
            tagParam = sharedTagParam.trim().ifEmpty { null },
            newplayer = resolveNewplayer(noSdkNewplayerMode, noSdkNewplayerBranchName, noSdkNewplayerLocalIP)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Config InStream",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        ConfigSectionCard(
            title = "Configuration"
        ) {
            InstreamField("MDTK", sharedMdtk, DEFAULT_IS_MDTK) { sharedMdtk = it; persistShared() }
            InstreamField("Zone", sharedZone, DEFAULT_IS_ZONE) { sharedZone = it; persistShared() }
            InstreamField("ID Video", sharedSrc, DEFAULT_IS_SRC) { sharedSrc = it; persistShared() }
            InstreamField("URL Referrer", sharedUrl, DEFAULT_IS_URL_REFERRER) { sharedUrl = it; persistShared() }
            InstreamField("Tag Param", sharedTagParam, "facultatif") { sharedTagParam = it; persistShared() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Consent String",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Switch(
                    checked = consentStringEnabled,
                    onCheckedChange = {
                        consentStringEnabled = it
                        persistShared()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DigiBlue,
                        uncheckedThumbColor = DigiTextSecondary,
                        uncheckedTrackColor = DigiCardBorder
                    )
                )
            }

            Text(
                text = "Forcer le mode de déclenchement",
                style = MaterialTheme.typography.labelMedium,
                color = DigiTextSecondary
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PLAY_MODES.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = sdkPlayMode == mode,
                        onClick = {
                            val newMode = if (sdkPlayMode == mode) null else mode
                            sdkPlayMode = newMode
                            InstreamPreferences.saveSdkConfig(context, newMode)
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, PLAY_MODES.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = DigiBlue.copy(alpha = 0.2f),
                            activeContentColor = DigiBlue,
                            activeBorderColor = DigiBlue,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = DigiTextSecondary,
                            inactiveBorderColor = DigiCardBorder
                        )
                    ) {
                        Text(PLAY_MODE_LABELS[i], style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mode Chromeless",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Switch(
                    checked = noSdkChromeless,
                    onCheckedChange = {
                        noSdkChromeless = it
                        persistNoSdk()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DigiBlue,
                        uncheckedThumbColor = DigiTextSecondary,
                        uncheckedTrackColor = DigiCardBorder
                    )
                )
            }

            Text(
                text = "Type de player",
                style = MaterialTheme.typography.labelMedium,
                color = DigiTextSecondary
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                NewplayerMode.entries.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = noSdkNewplayerMode == mode.name,
                        onClick = {
                            noSdkNewplayerMode = mode.name
                            persistNoSdk()
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, NewplayerMode.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = DigiBlue.copy(alpha = 0.2f),
                            activeContentColor = DigiBlue,
                            activeBorderColor = DigiBlue,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = DigiTextSecondary,
                            inactiveBorderColor = DigiCardBorder
                        )
                    ) {
                        Text(mode.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (noSdkNewplayerMode == NewplayerMode.RECETTE.name) {
                InstreamField("Nom de la branche sur Amplify", noSdkNewplayerBranchName, "ex: dev ou SUP-123") {
                    noSdkNewplayerBranchName = it
                    persistNoSdk()
                }
            }
            if (noSdkNewplayerMode == NewplayerMode.LOCAL.name) {
                InstreamField("Adresse IP", noSdkNewplayerLocalIP, "ex: 192.168.X.X:YYYY") {
                    noSdkNewplayerLocalIP = it
                    persistNoSdk()
                }
            }
        }

        ConfigSectionCard(
            title = "",
        ) {
            InstreamField("URL player (override)", noSdkCustomUrl, "Remplacement des paramètres précédents") {
                noSdkCustomUrl = it
                persistNoSdk()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "URL du player générée",
                style = MaterialTheme.typography.labelMedium,
                color = DigiTextSecondary
            )
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(noSdkUrlPreview))
                    copiedUrl = true
                    scope.launch {
                        delay(1200)
                        copiedUrl = false
                    }
                },
                border = BorderStroke(1.dp, DigiCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DigiBlue)
            ) {
                Text(
                    if (copiedUrl) "Copiée" else "Copier l'URL",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        SelectionContainer {
            Text(
                text = noSdkUrlPreview,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = DigiTextSecondary
            )
        }
        Text(
            text = "L'URL ci-dessus est reconstruite en temps réel à partir des valeurs saisies.",
            style = MaterialTheme.typography.labelSmall,
            color = DigiTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DigiCard),
        border = BorderStroke(1.dp, DigiCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = DigiBlue
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = DigiTextSecondary
                    )
                }
                HorizontalDivider(color = DigiCardBorder, thickness = 1.dp)
            }
            content()
        }
    }
}

@Composable
private fun InstreamField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DigiBlue,
            unfocusedBorderColor = DigiCardBorder,
            focusedLabelColor = DigiBlue,
            unfocusedLabelColor = DigiTextSecondary,
            cursorColor = DigiBlue,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedPlaceholderColor = DigiTextSecondary,
            focusedPlaceholderColor = DigiTextSecondary,
        )
    )
}
