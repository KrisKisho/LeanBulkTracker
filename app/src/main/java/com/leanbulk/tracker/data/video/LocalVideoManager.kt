package com.leanbulk.tracker.data.video

import android.content.Context
import android.os.Environment
import com.leanbulk.tracker.data.model.WorkoutDataSource
import java.io.File

object LocalVideoManager {

    /**
     * Resolves the local video file on device storage corresponding to the given videoId.
     * Returns null if no matching local video is found (prompting fallback to YouTube player).
     */
    fun findLocalVideo(context: Context, videoId: String): File? {
        val searchDirs = listOfNotNull(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            context.getExternalFilesDir(null)?.let { File(it, "Movies") },
            File("/sdcard/Android/data/${context.packageName}/files/Movies"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "LeanBulk"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        ).filter { it.exists() && it.isDirectory }

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
}
