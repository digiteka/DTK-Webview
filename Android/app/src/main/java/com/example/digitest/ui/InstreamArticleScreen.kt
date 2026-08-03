package com.example.digitest.ui

import android.app.Activity
import android.net.Uri
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.digiteka.instream.InStream
import com.digiteka.instream.core.config.Position
import com.digiteka.instream.core.config.player.DTKISMainPlayerConfig
import com.digiteka.instream.core.config.visibleplayer.DTKISVisiblePlayerConfig
import com.digiteka.instream.ui.player.MainPlayerView
import com.example.digitest.DEFAULT_IS_SRC
import com.example.digitest.DEFAULT_IS_URL_REFERRER
import com.example.digitest.DEFAULT_IS_ZONE
import com.example.digitest.InstreamPreferences
import com.example.digitest.consent.ConsentManager
import com.example.digitest.playModeFromString
import com.example.digitest.utils.destroyWebViewsRecursively
import com.example.digitest.utils.enableThirdPartyCookiesRecursively

@Composable
fun InstreamArticleScreen() {
    val context = LocalContext.current
    val tcString = remember { ConsentManager.getTcString(context) }

    // Hauteur de la navbar en dp — le visible player SDK se positionne par rapport
    // au bas de android.R.id.content qui, en edge-to-edge, inclut la zone navbar.
    val density = LocalDensity.current
    val navBarHeightDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp().value }

    val mainPlayerConfig = remember {
        val cfg = InstreamPreferences.getSdkConfig(context)
        DTKISMainPlayerConfig.Builder(
            zone = cfg.zone ?: DEFAULT_IS_ZONE,
            gdprConsentString = tcString ?: "",
            src = cfg.src ?: DEFAULT_IS_SRC,
            urlReferrer = Uri.encode(cfg.urlReferrer ?: DEFAULT_IS_URL_REFERRER),
            tagParam = null
        ).setPlayMode(playModeFromString(cfg.playMode)).build()
    }

    val mainPlayerView = remember { MainPlayerView(context) }
    val mainPlayerKey = remember {
        InStream.configureMainPlayer(mainPlayerView, mainPlayerConfig)
    }

    DisposableEffect(mainPlayerKey) {
        val activity = context as? Activity
        val rootView = activity
            ?.window
            ?.decorView
            ?.findViewById<FrameLayout>(android.R.id.content)

        // Snapshot child count avant d'attacher le visible player
        val childCountBefore = rootView?.childCount ?: 0

        if (rootView != null) {
            InStream.attachVisiblePlayerTo(
                parent = rootView,
                visiblePlayerConfig = DTKISVisiblePlayerConfig(
                    widthPercent = 0.65f,
                    position = Position.BOTTOM_END,
                    ratio = "16:9",
                    horizontalMargin = 12f,
                    verticalMargin = 12f + navBarHeightDp
                ),
                mainPlayerKey = mainPlayerKey
            )
        }

        onDispose {
            // Retirer et détruire le(s) visible player(s) ajoutés par InStream au rootView
            if (rootView != null) {
                val toRemove = (childCountBefore until rootView.childCount)
                    .map { rootView.getChildAt(it) }
                toRemove.forEach { child ->
                    destroyWebViewsRecursively(child)
                    rootView.removeView(child)
                }
            }
            // Détruire les WebViews du main player pour couper les requêtes réseau
            destroyWebViewsRecursively(mainPlayerView as android.view.View)
        }
    }

    // Active les cookies tiers sur les WebViews enfants dès qu'elles apparaissent dans le layout
    DisposableEffect(mainPlayerView) {
        val view = mainPlayerView as android.view.View
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            enableThirdPartyCookiesRecursively(view)
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        Text(
            text = "Article de presse",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = articleParagraph1,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph2,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // InStream MainPlayerView intégré au milieu de l'article (ratio 16:9)
        AndroidView(
            factory = { @Suppress("UNCHECKED_CAST") mainPlayerView as android.view.View },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph3,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph4,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph5,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph6,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph7,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph8,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph9,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph10,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph11,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph12,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = articleParagraph13,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        } // Column
    } // Surface
}

private const val articleParagraph1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."

private const val articleParagraph2 = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

private const val articleParagraph3 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."

private const val articleParagraph4 = "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet."

private const val articleParagraph5 = "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident."

private const val articleParagraph6 = "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint."

private const val articleParagraph7 = "Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat. Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur."

private const val articleParagraph8 = "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur."

private const val articleParagraph9 = "Sed haec quis possit intuentem Istius virtutem non admirari? Quae animi magnitudo in tam brevi vita tot res gestas includat! Mihi quidem Antimachus ille Colophoniis, magnus profecto poeta, magnus tamen et clarus orator, videtur aliquanto acrior in accusando quam in laudando."

private const val articleParagraph10 = "Nos autem de tota hac ratione in Hortensio satis, ut nobis quidem videtur, disputavimus. Sed haec hactenus; nunc ad institutum munus revertemur, ut de finibus bonorum et malorum aliquando dicamus. Qui autem de summo bono dissentit de tota philosophiae ratione dissentit."

private const val articleParagraph11 = "Certe, inquam, pertinax non ero tibique, si mihi probabis ea quae dicis, libenter adsentiar. Probabo, inquit, modo ista sis aequitate quam ostendis. Sed quoniam et advesperascit et mihi domum revertendum est, nunc quidem hactenus; de isto autem alias plura."

private const val articleParagraph12 = "Quod si ita se habeat, non possit beatam praestare vitam sapientia. Bonum integritas corporis erat, quod nunc in partes dividitur. Ut in geometria, prima si dederis, danda sunt omnia. Ego quoque, inquit Triarius, quod tibi difficile esset respondere, putavi non esse rogandus."

private const val articleParagraph13 = "Hoc loco tenere se Triarius non potuit. Tum Piso: Atqui, Cicero, inquit, haec in scholis Stoicorum exercitatio est quaedam, ut eos quos probant ea ipsa ex quibus probant refellant. Etenim non satis est illud dicere: non faciam mali quidquam, nam id etiam insipientes saepe faciunt."
