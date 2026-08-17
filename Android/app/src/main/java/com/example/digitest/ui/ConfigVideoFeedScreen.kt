package com.example.digitest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.digitest.DEFAULT_VF_CARROUSEL_HEIGHT_VH
import com.example.digitest.DEFAULT_VF_MDTK
import com.example.digitest.VideoFeedConfig
import com.example.digitest.VideoFeedPreferences
import com.example.digitest.ui.theme.DigiBlue
import com.example.digitest.ui.theme.DigiCardBorder
import com.example.digitest.ui.theme.DigiTextSecondary
import kotlinx.coroutines.launch

@Composable
fun VideoFeedConfigScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val initialConfig = remember { VideoFeedPreferences.getConfig(context) }
    var mdtkInput by rememberSaveable { mutableStateOf(initialConfig.mdtk ?: "") }
    var zoneIdInput by rememberSaveable { mutableStateOf(initialConfig.zoneId ?: "") }
    var adUnitPathInput by rememberSaveable { mutableStateOf(initialConfig.adUnitPath ?: "") }
    var videoIdInput by rememberSaveable { mutableStateOf(initialConfig.videoId ?: "") }
    var carrouselHeightInput by rememberSaveable { mutableStateOf(initialConfig.carrouselHeightVh ?: "") }
    var vfBranchMode by rememberSaveable { mutableStateOf(branchMode(initialConfig.vfBranch)) }
    var vfBranchInput by rememberSaveable { mutableStateOf(initialConfig.vfBranch ?: "") }
    var carrBranchMode by rememberSaveable { mutableStateOf(branchMode(initialConfig.carrBranch)) }
    var carrBranchInput by rememberSaveable { mutableStateOf(initialConfig.carrBranch ?: "") }
    var consentStringEnabledInput by rememberSaveable { mutableStateOf(initialConfig.consentStringEnabled) }
    var activeConfig by remember { mutableStateOf(initialConfig) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Configuration VideoFeed",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Text(
                text = "Configuration",
                style = MaterialTheme.typography.labelLarge,
                color = DigiBlue
            )

            OutlinedTextField(
                value = mdtkInput,
                onValueChange = { mdtkInput = it },
                label = { Text("MDTK") },
                placeholder = { Text(DEFAULT_VF_MDTK) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            OutlinedTextField(
                value = zoneIdInput,
                onValueChange = { zoneIdInput = it },
                label = { Text("Zone ID") },
                placeholder = { Text("optionnel") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            OutlinedTextField(
                value = adUnitPathInput,
                onValueChange = { adUnitPathInput = it },
                label = { Text("Ad Unit Path") },
                placeholder = { Text("/{networkCode}/{adBlockPath}") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            OutlinedTextField(
                value = videoIdInput,
                onValueChange = { videoIdInput = it },
                label = { Text("Video ID") },
                placeholder = { Text("optionnel (plein écran uniquement)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            OutlinedTextField(
                value = carrouselHeightInput,
                onValueChange = { input ->
                    carrouselHeightInput = input.filter { it.isDigit() }
                },
                label = { Text("Hauteur carrousel (vh)") },
                placeholder = { Text("$DEFAULT_VF_CARROUSEL_HEIGHT_VH (carrousel uniquement)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = consentStringEnabledInput,
                    onCheckedChange = { consentStringEnabledInput = it },
                    colors = CheckboxDefaults.colors(checkedColor = DigiBlue)
                )
                Text(
                    text = "Consent String",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "Environnements de test",
                style = MaterialTheme.typography.labelLarge,
                color = DigiBlue,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Pour des tests en local, installer le certificat utilisateur de l'ordinateur sur le téléphone utilisé.",
                style = MaterialTheme.typography.bodySmall,
                color = DigiTextSecondary
            )

            BranchModePicker(
                label = "VF_BRANCH",
                mode = vfBranchMode,
                onModeChange = { vfBranchMode = it; vfBranchInput = "" },
                input = vfBranchInput,
                onInputChange = { vfBranchInput = it },
                localPlaceholder = "ex: 192.168.1.136:5173"
            )

            BranchModePicker(
                label = "CARR_BRANCH",
                mode = carrBranchMode,
                onModeChange = { carrBranchMode = it; carrBranchInput = "" },
                input = carrBranchInput,
                onInputChange = { carrBranchInput = it },
                localPlaceholder = "ex: 192.168.1.136:5174"
            )

            Button(
                onClick = {
                    val config = VideoFeedConfig(
                        mdtk = mdtkInput.trim().ifEmpty { null },
                        zoneId = zoneIdInput.trim().ifEmpty { null },
                        adUnitPath = adUnitPathInput.trim().ifEmpty { null },
                        videoId = videoIdInput.trim().ifEmpty { null },
                        carrouselHeightVh = carrouselHeightInput.trim().ifEmpty { null },
                        vfBranch = if (vfBranchMode == BranchMode.PRODUCTION) null else vfBranchInput.trim().ifEmpty { null },
                        carrBranch = if (carrBranchMode == BranchMode.PRODUCTION) null else carrBranchInput.trim().ifEmpty { null },
                        consentStringEnabled = consentStringEnabledInput
                    )
                    VideoFeedPreferences.saveConfig(context, config)
                    activeConfig = VideoFeedPreferences.getConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Configuration sauvegardée") }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DigiBlue)
            ) {
                Text("Sauvegarder")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private enum class BranchMode(val label: String) {
    LOCAL("Local"),
    RECETTE("Recette"),
    PRODUCTION("Production")
}

/** Même heuristique que previewVideofeedHost()/preprodUrl() (verticalvideos/src/launcher/debug.ts:23) : une valeur IP LAN vaut Local, sinon Recette si renseignée, sinon Production. */
private fun branchMode(value: String?): BranchMode = when {
    value.isNullOrBlank() -> BranchMode.PRODUCTION
    value.startsWith("192.168") -> BranchMode.LOCAL
    else -> BranchMode.RECETTE
}

@Composable
private fun BranchModePicker(
    label: String,
    mode: BranchMode,
    onModeChange: (BranchMode) -> Unit,
    input: String,
    onInputChange: (String) -> Unit,
    localPlaceholder: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DigiTextSecondary
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            BranchMode.entries.forEach { entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeChange(entry) }
                ) {
                    RadioButton(
                        selected = mode == entry,
                        onClick = { onModeChange(entry) },
                        colors = RadioButtonDefaults.colors(selectedColor = DigiBlue)
                    )
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        if (mode != BranchMode.PRODUCTION) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        if (mode == BranchMode.LOCAL) localPlaceholder else "Branche Amplify, ex: SUP-132"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )
        }
    }
}

@Composable
private fun ConfigLine(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DigiTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
private fun configFieldColors() = OutlinedTextFieldDefaults.colors(
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
