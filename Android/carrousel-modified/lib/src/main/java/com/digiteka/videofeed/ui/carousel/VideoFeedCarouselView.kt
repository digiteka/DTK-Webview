package com.digiteka.videofeed.ui.carousel

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import com.digiteka.videofeed.R

/**
 * A carousel view that wraps [VideoFeedCarousel] with previous/next navigation arrow buttons
 * displayed below the carousel.
 *
 * Use [load] to start the carousel, and optionally set [onVideoClicked] to handle video taps.
 */
public class VideoFeedCarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val carousel = VideoFeedCarousel(context)

    /**
     * Callback invoked when a video thumbnail is clicked.
     * If null, the video opens in a fullscreen activity.
     */
    public var onVideoClicked: ((mdtk: String, videoId: String) -> Unit)?
        get() = carousel.onVideoClicked
        set(value) { carousel.onVideoClicked = value }

    /**
     * Ad unit path forwarded to the inner [VideoFeedCarousel].
     */
    public var adUnitPath: String?
        get() = carousel.adUnitPath
        set(value) { carousel.adUnitPath = value }

    init {
        orientation = VERTICAL

        addView(carousel, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        val navRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }

        val btnSize = (48 * context.resources.displayMetrics.density).toInt()

        val btnPrev = ImageButton(context).apply {
            setImageResource(R.drawable.ic_vf_carousel_prev)
            contentDescription = context.getString(R.string.vf_carousel_prev_description)
            background = null
            setOnClickListener { carousel.navigatePrevious() }
        }

        val btnNext = ImageButton(context).apply {
            setImageResource(R.drawable.ic_vf_carousel_next)
            contentDescription = context.getString(R.string.vf_carousel_next_description)
            background = null
            setOnClickListener { carousel.navigateNext() }
        }

        navRow.addView(btnPrev, LayoutParams(btnSize, btnSize))
        navRow.addView(btnNext, LayoutParams(btnSize, btnSize))
        addView(navRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    /**
     * Loads the carousel with the feed associated to the given MDTK.
     */
    public fun load(mdtk: String, adUnitPath: String? = null, zoneId: String? = null) {
        carousel.load(mdtk, adUnitPath, zoneId)
    }
}
