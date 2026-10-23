package com.leanbulk.tracker.ui.components

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.data.video.LocalVideoManager
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import java.io.File

/**
 * Universal video player that automatically prefers local offline video files via Media3 ExoPlayer
 * for zero-lag frame-accurate seeking and 100% offline playback.
 * If the local video file is not present on storage, it seamlessly falls back to YouTubePlayerView.
 */
@Composable
fun YouTubePlayerContainer(
    segment: WorkoutSegment?,
    onSegmentEnded: (WorkoutSegment) -> Unit,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    continuousPlay: Boolean = false,
    onToggleContinuousPlay: (Boolean) -> Unit = {}
) {
    WorkoutVideoPlayerContainer(
        segment = segment,
        onSegmentEnded = onSegmentEnded,
        modifier = modifier,
        autoPlay = autoPlay,
        continuousPlay = continuousPlay,
        onToggleContinuousPlay = onToggleContinuousPlay
    )
}

@Composable
fun WorkoutVideoPlayerContainer(
    segment: WorkoutSegment?,
    onSegmentEnded: (WorkoutSegment) -> Unit,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    continuousPlay: Boolean = false,
    onToggleContinuousPlay: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var forceYouTubeMode by remember { mutableStateOf(false) }

    // Check if local video file exists for this segment
    val localVideoFile: File? = remember(segment?.videoId, forceYouTubeMode) {
        if (segment == null || forceYouTubeMode) {
            null
        } else {
            LocalVideoManager.findLocalVideo(context, segment.videoId)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (localVideoFile != null) {
            LocalExoPlayerView(
                segment = segment,
                videoFile = localVideoFile,
                onSegmentEnded = onSegmentEnded,
                onFallbackToYouTube = { forceYouTubeMode = true },
                autoPlay = autoPlay,
                continuousPlay = continuousPlay,
                onToggleContinuousPlay = onToggleContinuousPlay
            )
        } else {
            YouTubeFallbackPlayerView(
                segment = segment,
                onSegmentEnded = onSegmentEnded,
                hasLocalOption = forceYouTubeMode,
                onReturnToLocal = { forceYouTubeMode = false },
                autoPlay = autoPlay,
                continuousPlay = continuousPlay,
                onToggleContinuousPlay = onToggleContinuousPlay
            )
        }
    }
}

/**
 * Native Media3 ExoPlayer container for playing local MP4 videos offline.
 */
@OptIn(UnstableApi::class)
@Composable
fun LocalExoPlayerView(
    segment: WorkoutSegment?,
    videoFile: File,
    onSegmentEnded: (WorkoutSegment) -> Unit,
    onFallbackToYouTube: () -> Unit,
    autoPlay: Boolean = false,
    continuousPlay: Boolean = false,
    onToggleContinuousPlay: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var loadedSegmentId by remember { mutableStateOf<String?>(null) }
    var hasTriggeredEndForCurrentSegment by remember { mutableStateOf(false) }

    // Prepare media and seek when segment changes
    LaunchedEffect(segment?.id, videoFile, autoPlay) {
        if (segment != null) {
            val isNewSegment = loadedSegmentId != segment.id
            if (isNewSegment) {
                loadedSegmentId = segment.id
                hasTriggeredEndForCurrentSegment = false
                val mediaItem = MediaItem.fromUri(Uri.fromFile(videoFile))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                val startMs = (segment.startSeconds * 1000).toLong()
                exoPlayer.seekTo(startMs)
            }

            if (autoPlay) {
                exoPlayer.play()
                isPlaying = true
            } else if (isNewSegment) {
                exoPlayer.playWhenReady = false
                isPlaying = false
            }
        }
    }

    // Monitor position and handle segment end vs continuous play
    LaunchedEffect(isPlaying, segment?.id, continuousPlay) {
        while (isPlaying && segment != null) {
            val currentSec = exoPlayer.currentPosition / 1000f
            if (currentSec >= segment.endSeconds && !hasTriggeredEndForCurrentSegment) {
                hasTriggeredEndForCurrentSegment = true
                onSegmentEnded(segment)
                if (!continuousPlay) {
                    // Only pause if continuous play is disabled
                    exoPlayer.pause()
                    isPlaying = false
                    break
                }
            }
            delay(150)
        }
    }

    // Sync ExoPlayer playback state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    segment?.let { onSegmentEnded(it) }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Lifecycle observer to pause when app is backgrounded
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Video Viewport with Badges Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .border(1.dp, NeonLime.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // High-contrast Offline Pill Overlay (Top Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .border(1.dp, NeonLime.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonLime)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "OFFLINE HD (NO ADS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            // Continuous Mode Toggle Pill (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (continuousPlay) ElectricCyan else DarkSurfaceBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleContinuousPlay(!continuousPlay) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (continuousPlay) Icons.Default.AllInclusive else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (continuousPlay) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (continuousPlay) "CONTINUOUS" else "STOP AT END",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (continuousPlay) ElectricCyan else TextSecondary,
                            fontSize = 9.sp,
                            letterSpacing = 0.4.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Play / Pause button
                Button(
                    onClick = {
                        if (isPlaying) {
                            exoPlayer.pause()
                            isPlaying = false
                        } else {
                            // If user explicitly presses Play, let it play forward naturally without resetting
                            exoPlayer.play()
                            isPlaying = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) AmberGold else NeonLime,
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

                // Restart Segment Button (Only this explicitly jumps back to startSeconds)
                OutlinedButton(
                    onClick = {
                        segment?.let { seg ->
                            hasTriggeredEndForCurrentSegment = false
                            exoPlayer.seekTo((seg.startSeconds * 1000).toLong())
                            exoPlayer.play()
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

                // Rewind 5s
                OutlinedButton(
                    onClick = {
                        val targetMs = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
                        exoPlayer.seekTo(targetMs)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FastRewind, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("-5s", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // Switch to YouTube Stream Option
            OutlinedButton(
                onClick = onFallbackToYouTube,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberGold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("YouTube", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

/**
 * YouTube iframe player fallback used when local video files are not found on device storage.
 */
@Composable
fun YouTubeFallbackPlayerView(
    segment: WorkoutSegment?,
    onSegmentEnded: (WorkoutSegment) -> Unit,
    hasLocalOption: Boolean = false,
    onReturnToLocal: () -> Unit = {},
    autoPlay: Boolean = false,
    continuousPlay: Boolean = false,
    onToggleContinuousPlay: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var activePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var loadedSegmentId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasTriggeredEndForCurrentSegment by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
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
                                segment?.let { seg ->
                                    loadedSegmentId = seg.id
                                    hasTriggeredEndForCurrentSegment = false
                                    if (autoPlay) {
                                        youTubePlayer.loadVideo(seg.videoId, seg.startSeconds)
                                        isPlaying = true
                                    } else {
                                        youTubePlayer.cueVideo(seg.videoId, seg.startSeconds)
                                        isPlaying = false
                                    }
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
                                    if (second >= currentSeg.endSeconds && !hasTriggeredEndForCurrentSegment) {
                                        hasTriggeredEndForCurrentSegment = true
                                        onSegmentEnded(currentSeg)
                                        if (!continuousPlay) {
                                            youTubePlayer.pause()
                                            isPlaying = false
                                        }
                                    }
                                }
                            }
                        }, options)
                    }
                },
                update = { view ->
                    if (segment != null && activePlayer != null) {
                        if (loadedSegmentId != segment.id) {
                            loadedSegmentId = segment.id
                            hasTriggeredEndForCurrentSegment = false
                            if (autoPlay) {
                                activePlayer?.loadVideo(segment.videoId, segment.startSeconds)
                                isPlaying = true
                            } else {
                                activePlayer?.cueVideo(segment.videoId, segment.startSeconds)
                                isPlaying = false
                            }
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

            // Online Badge (Top Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "YOUTUBE STREAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            // Continuous Mode Toggle Pill (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (continuousPlay) ElectricCyan else DarkSurfaceBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleContinuousPlay(!continuousPlay) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (continuousPlay) Icons.Default.AllInclusive else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (continuousPlay) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (continuousPlay) "CONTINUOUS" else "STOP AT END",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (continuousPlay) ElectricCyan else TextSecondary,
                            fontSize = 9.sp,
                            letterSpacing = 0.4.sp
                        )
                    )
                }
            }
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
                                player.play()
                                isPlaying = true
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
                            hasTriggeredEndForCurrentSegment = false
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

                if (hasLocalOption) {
                    // Button to switch back to Local Offline Video
                    OutlinedButton(
                        onClick = onReturnToLocal,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonLime),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp), tint = NeonLime)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Use Local HD", fontSize = 11.sp, color = NeonLime)
                    }
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberGold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open YouTube", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
