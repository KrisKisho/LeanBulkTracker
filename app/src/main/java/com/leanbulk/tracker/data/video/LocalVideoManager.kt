package com.leanbulk.tracker.data.video

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.leanbulk.tracker.data.model.WorkoutDataSource
import java.io.File
import java.io.FileOutputStream

data class VideoFileStatus(
    val videoId: String,
    val title: String,
    val defaultFileName: String,
    val file: File?,
    val isFound: Boolean,
    val formattedSize: String
)

object LocalVideoManager {

    val TARGET_VIDEOS = listOf(
        WorkoutDataSource.VIDEO_CHEST to ("Chest Hypertrophy (30m)" to "chest.mp4"),
        WorkoutDataSource.VIDEO_ARMS to ("Arms Killer (20m)" to "arms.mp4"),
        WorkoutDataSource.VIDEO_FULL_BODY to ("Intense Full Body (30m)" to "full_body.mp4")
    )

    /**
     * Resolves the local video file on device storage corresponding to the given videoId.
     * Returns null if no matching local video is found (prompting fallback to YouTube player).
     */
    fun findLocalVideo(context: Context, videoId: String): File? {
        val searchDirs = getSearchDirectories(context)

        val targetKeywords: List<String> = when (videoId) {
            WorkoutDataSource.VIDEO_CHEST -> listOf("chest", "l5yuivJYj5k")
            WorkoutDataSource.VIDEO_ARMS -> listOf("arm", "mcuFTrNDLkg")
            WorkoutDataSource.VIDEO_FULL_BODY -> listOf("full_body", "fullbody", "full body", "udbU6ykFFGE")
            else -> listOf(videoId.lowercase())
        }

        // 1. Direct exact filename check first
        for (dir in searchDirs) {
            for (kw in targetKeywords) {
                val directFile = File(dir, "$kw.mp4")
                if (directFile.exists() && directFile.length() > 1024 * 1024) {
                    return directFile
                }
            }
        }

        // 2. Fuzzy search inside directories
        for (dir in searchDirs) {
            val files = dir.listFiles { file ->
                file.isFile && file.name.endsWith(".mp4", ignoreCase = true) && file.length() > 1024 * 1024
            } ?: continue

            for (file in files) {
                val lowerName = file.name.lowercase()
                if (targetKeywords.any { kw -> lowerName.contains(kw) }) {
                    return file
                }
            }
        }

        return null
    }

    /**
     * Returns the target folder where users can place videos for 100% offline playback.
     */
    fun getSuggestedStoragePath(context: Context): String {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return dir?.absolutePath ?: "/sdcard/Android/data/${context.packageName}/files/Movies"
    }

    /**
     * All directories scanned for workout videos on the phone.
     */
    fun getSearchDirectories(context: Context): List<File> {
        return listOfNotNull(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            context.getExternalFilesDir(null)?.let { File(it, "Movies") },
            File("/sdcard/Android/data/${context.packageName}/files/Movies"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "LeanBulk"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        ).filter { it.exists() && it.isDirectory }
    }

    /**
     * Returns live status of all 3 required workout videos.
     */
    fun getVideoStatusList(context: Context): List<VideoFileStatus> {
        return TARGET_VIDEOS.map { (vId, meta) ->
            val (name, defaultFileName) = meta
            val file = findLocalVideo(context, vId)
            val sizeFormatted = if (file != null && file.exists() && file.length() > 1024 * 1024) {
                "%.1f MB".format(file.length() / (1024f * 1024f))
            } else {
                "Not Found"
            }
            VideoFileStatus(
                videoId = vId,
                title = name,
                defaultFileName = defaultFileName,
                file = file,
                isFound = file != null && file.exists() && file.length() > 1024 * 1024,
                formattedSize = sizeFormatted
            )
        }
    }

    /**
     * Imports a video file from content Uri into the app's offline movies directory.
     */
    fun importVideoFile(context: Context, sourceUri: Uri, targetVideoId: String): Boolean {
        return try {
            val destDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: return false
            if (!destDir.exists()) destDir.mkdirs()

            val fileName = when (targetVideoId) {
                WorkoutDataSource.VIDEO_CHEST -> "chest.mp4"
                WorkoutDataSource.VIDEO_ARMS -> "arms.mp4"
                WorkoutDataSource.VIDEO_FULL_BODY -> "full_body.mp4"
                else -> "${targetVideoId}.mp4"
            }

            val destFile = File(destDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.exists() && destFile.length() > 1024 * 1024
        } catch (_: Exception) {
            false
        }
    }
}
