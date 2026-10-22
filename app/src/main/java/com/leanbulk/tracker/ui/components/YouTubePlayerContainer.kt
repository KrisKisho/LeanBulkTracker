package com.leanbulk.tracker.ui.components

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayerContainer(
    segment: WorkoutSegment?,
    onSegmentEnded: (WorkoutSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var activePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var loadedSegmentId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        enableAutomaticInitialization = false
                        lifecycleOwner.lifecycle.addObserver(this)

                        val options = IFramePlayerOptions.Builder(ctx)
                            .controls(1)
                            .fullscreen(1)
                            .autoplay(0)
                            .rel(0)
                            .build()

                        initialize(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                activePlayer = youTubePlayer
                                playerError = null
                                segment?.let { seg ->
                                    loadedSegmentId = seg.id
                                    youTubePlayer.cueVideo(seg.videoId, seg.startSeconds)
                                    isPlaying = false
                                }
                            }

                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: PlayerConstants.PlayerState
                            ) {
                                isPlaying = (state == PlayerConstants.PlayerState.PLAYING)
                                if (state == PlayerConstants.PlayerState.ENDED) {
                                    segment?.let { onSegmentEnded(it) }
                                }
                            }

                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                segment?.let { currentSeg ->
                                    if (second >= currentSeg.endSeconds) {
                                        youTubePlayer.pause()
                                        isPlaying = false
                                        onSegmentEnded(currentSeg)
                                    }
                                }
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: PlayerConstants.PlayerError
                            ) {
                                playerError = "Playback issue: ${error.name}"
                            }
                        }, options)
                    }
                },
                update = { view ->
                    if (segment != null && activePlayer != null) {
                        if (loadedSegmentId != segment.id) {
                            loadedSegmentId = segment.id
                            playerError = null
                            activePlayer?.cueVideo(segment.videoId, segment.startSeconds)
                            isPlaying = false
                        }
                    }
                },
                onRelease = { view ->
                    try {
                        lifecycleOwner.lifecycle.removeObserver(view)
                        view.release()
                    } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Controls & Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Play / Pause button
                Button(
                    onClick = {
                        activePlayer?.let { player ->
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                segment?.let { seg ->
                                    player.play()
                                    isPlaying = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) AmberGold else ElectricCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "Pause" else "Play",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Restart / Jump to Segment start
                OutlinedButton(
                    onClick = {
                        segment?.let { seg ->
                            activePlayer?.seekTo(seg.startSeconds)
                            activePlayer?.play()
                            isPlaying = true
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Direct YouTube Fallback Button
            OutlinedButton(
                onClick = {
                    if (segment != null) {
                        val url = "https://www.youtube.com/watch?v=${segment.videoId}&t=${segment.startSeconds.toInt()}s"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberGold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open YouTube", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
