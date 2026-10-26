package com.leanbulk.tracker.data.model

import java.time.DayOfWeek

object BellyFatCircuitCatalog {

    const val VIDEO_ID = WorkoutDataSource.VIDEO_BELLY_FAT

    const val STOMACH_VACUUM_CUE = "Stomach Vacuum Cue: Exhale completely and pull your navel in toward your spine during the entire 45 seconds to tighten your inner abdominal wall."

    val ALL_SEGMENTS = listOf(
        WorkoutSegment(
            id = "bf_seg_1",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 1,
            title = "1. Jumping Jacks (45s)",
            videoId = VIDEO_ID,
            startSeconds = 64f,
            endSeconds = 128f,
            note = "Metabolic warmup & heart rate elevation (45s active / 15s rest)"
        ),
        WorkoutSegment(
            id = "bf_seg_2",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 2,
            title = "2. Mountain Climbers (45s)",
            videoId = VIDEO_ID,
            startSeconds = 131f,
            endSeconds = 206f,
            note = "High core engagement; keep shoulders steady over wrists"
        ),
        WorkoutSegment(
            id = "bf_seg_3",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 3,
            title = "3. Isometric Elbow Plank (45s)",
            videoId = VIDEO_ID,
            startSeconds = 207f,
            endSeconds = 278f,
            note = STOMACH_VACUUM_CUE
        ),
        WorkoutSegment(
            id = "bf_seg_4",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 4,
            title = "4. Seated Flutter Kicks (45s)",
            videoId = VIDEO_ID,
            startSeconds = 283f,
            endSeconds = 350f,
            note = "Target lower rectus abdominis; lean torso back 45 degrees"
        ),
        WorkoutSegment(
            id = "bf_seg_5",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 5,
            title = "5. High Knees / High Knee March (45s)",
            videoId = VIDEO_ID,
            startSeconds = 354f,
            endSeconds = 416f,
            note = "Metabolic burn; drive knees toward waist level"
        ),
        WorkoutSegment(
            id = "bf_seg_6",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 6,
            title = "6. Seated Russian Twists (45s)",
            videoId = VIDEO_ID,
            startSeconds = 420f,
            endSeconds = 492f,
            note = "Rotate shoulders side to side; cinch the waistline"
        ),
        WorkoutSegment(
            id = "bf_seg_7",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 7,
            title = "7. Seated Leg Tuck-Ins (45s)",
            videoId = VIDEO_ID,
            startSeconds = 499f,
            endSeconds = 564f,
            note = STOMACH_VACUUM_CUE
        ),
        WorkoutSegment(
            id = "bf_seg_8",
            dayOfWeek = DayOfWeek.MONDAY,
            orderIndex = 8,
            title = "8. Bicycle Crunches (45s)",
            videoId = VIDEO_ID,
            startSeconds = 602f,
            endSeconds = 668f,
            note = "Final core burn (auto-skips 565s–601s mid-video promo)"
        )
    )

    fun resolveVariant(variantSetting: String, dayOfWeek: DayOfWeek): String {
        return if (variantSetting.equals("AUTO", ignoreCase = true)) {
            if (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.THURSDAY) {
                "PURE_ABS_6_MOVES"
            } else {
                "FULL_8_MOVES"
            }
        } else {
            variantSetting
        }
    }

    fun getSegments(variantSetting: String, dayOfWeek: DayOfWeek): List<WorkoutSegment> {
        val resolved = resolveVariant(variantSetting, dayOfWeek)
        return if (resolved.equals("PURE_ABS_6_MOVES", ignoreCase = true)) {
            val allowedIds = setOf("bf_seg_2", "bf_seg_3", "bf_seg_4", "bf_seg_6", "bf_seg_7", "bf_seg_8")
            ALL_SEGMENTS.filter { it.id in allowedIds }.mapIndexed { index, seg ->
                seg.copy(orderIndex = index + 1, dayOfWeek = dayOfWeek)
            }
        } else {
            ALL_SEGMENTS.mapIndexed { index, seg ->
                seg.copy(orderIndex = index + 1, dayOfWeek = dayOfWeek)
            }
        }
    }
}
