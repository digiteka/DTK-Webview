package com.example.digitest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.digitest.DEFAULT_IS_MDTK
import com.example.digitest.DEFAULT_IS_PLAY_MODE
import com.example.digitest.DEFAULT_IS_SRC
import com.example.digitest.DEFAULT_IS_URL_REFERRER
import com.example.digitest.DEFAULT_IS_ZONE
import com.example.digitest.InstreamPreferences
import com.example.digitest.InstreamSharedConfig
import com.example.digitest.ui.theme.DigiBlue
import com.example.digitest.ui.theme.DigiCardBorder
import com.example.digitest.ui.theme.DigiTextSecondary
import kotlinx.coroutines.launch

private val PLAY_MODES = listOf("ON_CLICK", "VISIBLE_AT_FIFTY_PERCENT", "AUTOPLAY")
private val PLAY_MODE_LABELS = listOf("Au clic", "50% visible", "Auto")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstreamConfigScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── Paramètres communs ──────────────────────────────────────────────
    val initShared = remember { InstreamPreferences.getSharedConfig(context) }
    var sharedMdtk by rememberSaveable { mutableStateOf(initShared.mdtk ?: "") }
    var sharedZone by rememberSaveable { mutableStateOf(initShared.zone ?: "") }
    var sharedSrc by rememberSaveable { mutableStateOf(initShared.src ?: "") }
    var sharedUrl by rememberSaveable { mutableStateOf(initShared.urlReferrer ?: "") }
    var activeShared by remember { mutableStateOf(initShared) }

    // ── SDK InStream — play mode uniquement ─────────────────────────────
    val initSdk = remember { InstreamPreferences.getSdkConfig(context) }
    var sdkPlayMode by rememberSaveable { mutableStateOf(initSdk.playMode ?: DEFAULT_IS_PLAY_MODE) }
    var activeSdkPlayMode by remember { mutableStateOf(initSdk.playMode ?: DEFAULT_IS_PLAY_MODE) }

    // ── InStream sans SDK — options spécifiques ─────────────────────────
    val initNoSdk = remember { InstreamPreferences.getNoSdkConfig(context) }
    var noSdkChromeless by rememberSaveable { mutableStateOf(initNoSdk.chromeless) }
    var noSdkCustomUrl by rememberSaveable { mutableStateOf(initNoSdk.customUrl ?: "") }
    var activeNoSdk by remember { mutableStateOf(initNoSdk) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Config InStream",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            // ── Section Paramètres communs ──────────────────────────────
            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle("Paramètres communs")

            Text(
                text = "Appliqués aux deux intégrations SDK et sans SDK",
                style = MaterialTheme.typography.labelSmall,
                color = DigiTextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            InstreamField("MDTK", sharedMdtk, DEFAULT_IS_MDTK) { sharedMdtk = it }
            Text(
                text = "Changement de MDTK effectif au prochain démarrage de l'app",
                style = MaterialTheme.typography.labelSmall,
                color = DigiTextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            InstreamField("Zone", sharedZone, DEFAULT_IS_ZONE) { sharedZone = it }
            InstreamField("Src (Video ID)", sharedSrc, DEFAULT_IS_SRC) { sharedSrc = it }
            InstreamField("URL Référent", sharedUrl, DEFAULT_IS_URL_REFERRER) { sharedUrl = it }

            SaveResetRow(
                onSave = {
                    InstreamPreferences.saveSharedConfig(
                        context, InstreamSharedConfig(
                            mdtk = sharedMdtk.trim().ifEmpty { null },
                            zone = sharedZone.trim().ifEmpty { null },
                            src = sharedSrc.trim().ifEmpty { null },
                            urlReferrer = sharedUrl.trim().ifEmpty { null }
                        )
                    )
                    activeShared = InstreamPreferences.getSharedConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Paramètres communs sauvegardés") }
                },
                onReset = {
                    InstreamPreferences.resetSharedConfig(context)
                    sharedMdtk = ""; sharedZone = ""; sharedSrc = ""; sharedUrl = ""
                    activeShared = InstreamPreferences.getSharedConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Paramètres communs réinitialisés") }
                }
            )

            ActiveInstreamConfig(
                "MDTK" to (activeShared.mdtk ?: "$DEFAULT_IS_MDTK (défaut)"),
                "Zone" to (activeShared.zone ?: "$DEFAULT_IS_ZONE (défaut)"),
                "Src" to (activeShared.src ?: "$DEFAULT_IS_SRC (défaut)"),
                "URL" to (activeShared.urlReferrer ?: "$DEFAULT_IS_URL_REFERRER (défaut)")
            )

            // ── Section SDK ─────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("SDK InStream")

            Text(
                text = "Play Mode",
                style = MaterialTheme.typography.labelMedium,
                color = DigiTextSecondary
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PLAY_MODES.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = sdkPlayMode == mode,
                        onClick = { sdkPlayMode = mode },
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

            SaveResetRow(
                onSave = {
                    InstreamPreferences.saveSdkConfig(context, sdkPlayMode)
                    activeSdkPlayMode = sdkPlayMode
                    scope.launch { snackbarHostState.showSnackbar("Config SDK sauvegardée") }
                },
                onReset = {
                    InstreamPreferences.resetSdkConfig(context)
                    sdkPlayMode = DEFAULT_IS_PLAY_MODE
                    activeSdkPlayMode = DEFAULT_IS_PLAY_MODE
                    scope.launch { snackbarHostState.showSnackbar("Config SDK réinitialisée") }
                }
            )

            ActiveInstreamConfig(
                "Play Mode" to activeSdkPlayMode
            )

            // ── Section Sans SDK ────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("InStream sans SDK")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chromeless",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Switch(
                    checked = noSdkChromeless,
                    onCheckedChange = { noSdkChromeless = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DigiBlue,
                        uncheckedThumbColor = DigiTextSecondary,
                        uncheckedTrackColor = DigiCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = DigiCardBorder, thickness = 1.dp)
            InstreamField("URL player (override)", noSdkCustomUrl, "https://...") { noSdkCustomUrl = it }
            Text(
                text = "Si rempli, remplace entièrement l'URL construite — tous les autres paramètres sont ignorés",
                style = MaterialTheme.typography.labelSmall,
                color = DigiTextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            SaveResetRow(
                onSave = {
                    InstreamPreferences.saveNoSdkConfig(
                        context,
                        chromeless = noSdkChromeless,
                        customUrl = noSdkCustomUrl.trim().ifEmpty { null }
                    )
                    activeNoSdk = InstreamPreferences.getNoSdkConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Config sans SDK sauvegardée") }
                },
                onReset = {
                    InstreamPreferences.resetNoSdkConfig(context)
                    noSdkChromeless = false; noSdkCustomUrl = ""
                    activeNoSdk = InstreamPreferences.getNoSdkConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Config sans SDK réinitialisée") }
                }
            )

            ActiveInstreamConfig(
                "Chromeless" to if (activeNoSdk.chromeless) "ON" else "OFF",
                "URL override" to (activeNoSdk.customUrl ?: "(aucune)")
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = DigiBlue
    )
    HorizontalDivider(color = DigiCardBorder, thickness = 1.dp)
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

@Composable
private fun SaveResetRow(onSave: () -> Unit, onReset: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = DigiBlue)
        ) { Text("Sauvegarder") }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            border = BorderStroke(1.dp, DigiCardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DigiTextSecondary)
        ) { Text("Réinitialiser") }
    }
}

@Composable
private fun ActiveInstreamConfig(vararg pairs: Pair<String, String>) {
    Text(
        text = "Configuration active :",
        style = MaterialTheme.typography.labelLarge,
        color = DigiBlue,
        modifier = Modifier.padding(top = 4.dp)
    )
    pairs.forEach { (label, value) ->
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = DigiTextSecondary)
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}
