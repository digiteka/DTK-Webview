package com.example.digitest.ui

import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val DOMAIN = ".ultimedia.com"
private const val DOMAIN_URL = "https://ultimedia.com"

@Composable
fun CookieManagerScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var cookieName by remember { mutableStateOf("") }
    var cookieValue by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf(getCookies()) }

    fun refresh() { cookies = getCookies() }

    Scaffold(
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cookies — $DOMAIN",
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = cookieName,
                    onValueChange = { cookieName = it },
                    label = { Text("Nom") },
                    placeholder = { Text("debug") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cookieValue,
                    onValueChange = { cookieValue = it },
                    label = { Text("Valeur") },
                    placeholder = { Text("true") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (cookieName.isNotBlank()) {
                        setCookie(cookieName.trim(), cookieValue.trim())
                        refresh()
                        cookieName = ""
                        cookieValue = ""
                        scope.launch { snackbarHostState.showSnackbar("Cookie ajouté") }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter")
            }

            HorizontalDivider()

            Text(
                text = "Cookies actifs (${cookies.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cookies, key = { it.first }) { (name, value) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "$name=$value",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            removeCookie(name)
                            refresh()
                            scope.launch { snackbarHostState.showSnackbar("Cookie supprimé") }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }

            OutlinedButton(
                onClick = {
                    removeAllCookies()
                    refresh()
                    scope.launch { snackbarHostState.showSnackbar("Tous les cookies supprimés") }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tout supprimer")
            }
        }
    }
}

private fun setCookie(name: String, value: String) {
    val cm = CookieManager.getInstance()
    cm.setAcceptCookie(true)
    cm.setCookie(DOMAIN_URL, "$name=$value; Domain=$DOMAIN; Path=/; SameSite=None; Secure")
    cm.flush()
}

private fun getCookies(): List<Pair<String, String>> {
    val cookieString = CookieManager.getInstance().getCookie(DOMAIN_URL) ?: return emptyList()
    return cookieString.split(";")
        .map { it.trim() }
        .filter { it.contains("=") }
        .map {
            val parts = it.split("=", limit = 2)
            parts[0].trim() to (parts.getOrNull(1)?.trim() ?: "")
        }
}

private fun removeCookie(name: String) {
    val cm = CookieManager.getInstance()
    cm.setCookie(DOMAIN_URL, "$name=; Domain=$DOMAIN; Path=/; Max-Age=0; SameSite=None; Secure")
    cm.flush()
}

private fun removeAllCookies() {
    val cm = CookieManager.getInstance()
    cm.removeAllCookies(null)
    cm.flush()
}
