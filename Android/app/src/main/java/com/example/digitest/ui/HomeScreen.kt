package com.example.digitest.ui

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.digiteka.videofeed.ui.VideoFeedActivity
import com.example.digitest.DEFAULT_VF_MDTK
import com.example.digitest.R
import com.example.digitest.VideoFeedPreferences
import com.example.digitest.ui.theme.DigiBlue
import com.example.digitest.ui.theme.DigiCard
import com.example.digitest.ui.theme.DigiCardBorder
import com.example.digitest.ui.theme.DigiTextSecondary

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            painter = painterResource(R.drawable.logo_digiteka),
            contentDescription = "Digiteka",
            tint = Color.Unspecified,
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(58.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SDK Test App",
            style = MaterialTheme.typography.labelLarge,
            color = DigiTextSecondary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        HomeCard(
            title = "SDK Instream",
            subtitle = "Lecteur vidéo flottant dans un article",
            icon = Icons.Default.PlayArrow,
            onClick = { navController.navigate("instream") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        HomeCard(
            title = "Instream sans SDK",
            subtitle = "Intégration WebView iframe Ultimedia",
            icon = Icons.Default.PlayArrow,
            onClick = { navController.navigate("nosdk") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        HomeCard(
            title = "VideoFeed Carousel",
            subtitle = "Carousel vidéo horizontal",
            icon = Icons.Default.PlayArrow,
            onClick = { navController.navigate("carousel") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        HomeCard(
            title = "VideoFeed Plein Écran",
            subtitle = "Lecteur vertical style TikTok",
            icon = Icons.Default.PlayArrow,
            onClick = { launchVideoFeedActivity(context) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        HomeCard(
            title = "Debug Cookies",
            subtitle = "Gérer les cookies .ultimedia.com",
            icon = Icons.Default.Build,
            onClick = { navController.navigate("cookies") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        HomeCard(
            title = "Config InStream",
            subtitle = "Paramètres SDK et sans SDK InStream",
            icon = Icons.Default.Settings,
            onClick = { navController.navigate("instream_config") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        HomeCard(
            title = "Config VideoFeed",
            subtitle = "Paramètres MDTK, Zone, Ad Unit, Video ID",
            icon = Icons.Default.Settings,
            onClick = { navController.navigate("videofeed_config") }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DigiCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DigiCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DigiBlue.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DigiBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DigiTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DigiCardBorder,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun launchVideoFeedActivity(context: Context) {
    val config = VideoFeedPreferences.getConfig(context)
    context.startActivity(
        VideoFeedActivity.newInstance(
            context = context,
            mdtk = config.mdtk ?: DEFAULT_VF_MDTK,
            videoId = config.videoId,
            adUnitPath = config.adUnitPath,
            zoneId = config.zoneId
        )
    )
}
