package com.example.digitest.ui

import android.webkit.CookieManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    var vfBranchInput by rememberSaveable { mutableStateOf(initialConfig.vfBranch ?: "") }
    var carrBranchInput by rememberSaveable { mutableStateOf(initialConfig.carrBranch ?: "") }
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
                text = "Config VideoFeed",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
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

            OutlinedTextField(
                value = vfBranchInput,
                onValueChange = { vfBranchInput = it },
                label = { Text("VF_BRANCH") },
                placeholder = { Text("Nom de la branche Amplify (SUP-132, EVO-456....) ou local") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            OutlinedTextField(
                value = carrBranchInput,
                onValueChange = { carrBranchInput = it },
                label = { Text("CARR_BRANCH") },
                placeholder = { Text("Nom de la branche Amplify (SUP-132, EVO-456....) ou local") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = configFieldColors()
            )

            Button(
                onClick = {
                    val config = VideoFeedConfig(
                        mdtk = mdtkInput.trim().ifEmpty { null },
                        zoneId = zoneIdInput.trim().ifEmpty { null },
                        adUnitPath = adUnitPathInput.trim().ifEmpty { null },
                        videoId = videoIdInput.trim().ifEmpty { null },
                        carrouselHeightVh = carrouselHeightInput.trim().ifEmpty { null },
                        vfBranch = vfBranchInput.trim().ifEmpty { null },
                        carrBranch = carrBranchInput.trim().ifEmpty { null }
                    )
                    VideoFeedPreferences.saveConfig(context, config)
                    if (config.carrBranch.equals("local", ignoreCase = true)) {
                        setLocalIpCarrCookie()
                    }
                    activeConfig = VideoFeedPreferences.getConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Configuration sauvegardée") }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DigiBlue)
            ) {
                Text("Sauvegarder")
            }

            OutlinedButton(
                onClick = {
                    VideoFeedPreferences.resetConfig(context)
                    mdtkInput = ""
                    zoneIdInput = ""
                    adUnitPathInput = ""
                    videoIdInput = ""
                    carrouselHeightInput = ""
                    vfBranchInput = ""
                    carrBranchInput = ""
                    activeConfig = VideoFeedPreferences.getConfig(context)
                    scope.launch { snackbarHostState.showSnackbar("Configuration réinitialisée") }
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, DigiCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DigiTextSecondary)
            ) {
                Text("Réinitialiser")
            }

            Text(
                text = "Configuration active :",
                style = MaterialTheme.typography.labelLarge,
                color = DigiBlue,
                modifier = Modifier.padding(top = 4.dp)
            )

            ConfigLine("MDTK", activeConfig.mdtk ?: "$DEFAULT_VF_MDTK (défaut)")
            ConfigLine("Zone ID", activeConfig.zoneId ?: "—")
            ConfigLine("Ad Unit Path", activeConfig.adUnitPath ?: "—")
            ConfigLine("Video ID", activeConfig.videoId ?: "—")
            ConfigLine("Hauteur carrousel (vh)", activeConfig.carrouselHeightVh ?: "$DEFAULT_VF_CARROUSEL_HEIGHT_VH (défaut)")
            ConfigLine("VF_BRANCH", activeConfig.vfBranch ?: "—")
            ConfigLine("CARR_BRANCH", activeConfig.carrBranch ?: "—")

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// Le launcher VideoFeed lit ce cookie sur .digiteka.com (document.cookie) quand
// CARR_BRANCH vaut "local", pour pointer vers le serveur dev via IP LAN plutôt
// que localhost (inaccessible depuis un device/émulateur).
private fun setLocalIpCarrCookie() {
    val cm = CookieManager.getInstance()
    cm.setAcceptCookie(true)
    cm.setCookie(
        "https://digiteka.com",
        "localIP_CARR=192.168.1.136; Domain=.digiteka.com; Path=/; SameSite=None; Secure"
    )
    cm.flush()
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
