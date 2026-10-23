package com.leanbulk.tracker.data.model

import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity

/**
 * 4-Week Mesocycle Cadence & Progressive Overload Engine.
 *
 * Implements:
 * 1. 4-Week Double Progression Tempo Rules (Neuromuscular Groove, Rep Density, Piston Tension, Graduation Test)
 * 2. Movement Category Dumbbell Weight Ladder (+1.5kg to +5.0kg)
 * 3. 40-Second Window Exercise Target Rep Ranges
 * 4. 3-Signal "Weight Upgrade Unlocked" Verification Logic
 */
enum class MesocyclePhase(
    val weekNumber: Int,
    val phaseName: String,
    val tempoCode: String,
    val tempoDescription: String,
    val cadenceSummary: String,
    val focusDescription: String
) {
    WEEK_1(
        weekNumber = 1,
        phaseName = "Neuromuscular Groove",
        tempoCode = "3-1-1",
        tempoDescription = "3s down • 1s pause • 1s up (~5s/rep)",
        cadenceSummary = "3s lower, 1s pause, 1s lift",
        focusDescription = "Ultra-slow strict form. Establish neuromuscular control & eliminate momentum."
    ),
    WEEK_2(
        weekNumber = 2,
        phaseName = "Rep Density Expansion",
        tempoCode = "2-1-1",
        tempoDescription = "2s down • 1s peak squeeze • 1s up (~4s/rep)",
        cadenceSummary = "2s lower, 1s peak squeeze, 1s lift",
        focusDescription = "Controlled volume. Expand repetition count inside 40s work intervals."
    ),
    WEEK_3(
        weekNumber = 3,
        phaseName = "Continuous Tension Overload",
        tempoCode = "2-0-1",
        tempoDescription = "2s down • 0s rest • 1s up (~3s/rep)",
        cadenceSummary = "2s lower, 0s bottom rest, 1s lift",
        focusDescription = "Non-stop piston tension. Continuous mechanical strain and lactate accumulation."
    ),
    WEEK_4(
        weekNumber = 4,
        phaseName = "Graduation Test Week",
        tempoCode = "Max Controlled + Pulses",
        tempoDescription = "Max Controlled + Final 10s Halfway Pulses",
        cadenceSummary = "Explosive controlled + final 10s pulse burn",
        focusDescription = "Hit the Week 4 target on Set 3 to unlock the next dumbbell weight ladder step!"
    );

    companion object {
        fun fromWeek(week: Int): MesocyclePhase {
            return entries.firstOrNull { it.weekNumber == week } ?: WEEK_1
        }
    }
}

enum class MovementCategory(
    val displayName: String,
    val ladder: List<Float>,
    val defaultIncrement: Float
) {
    SMALL_ISOLATION(
        displayName = "Small Isolation",
        ladder = listOf(4.0f, 5.5f, 7.5f, 9.5f),
        defaultIncrement = 1.5f
    ),
    MEDIUM_UPPER(
        displayName = "Medium Upper",
        ladder = listOf(4.0f, 6.0f, 8.5f, 11.5f),
        defaultIncrement = 2.0f
    ),
    LARGE_UPPER(
        displayName = "Large Upper",
        ladder = listOf(4.0f, 7.5f, 10.5f, 14.0f),
        defaultIncrement = 3.0f
    ),
    LOWER_BODY(
        displayName = "Lower Body",
        ladder = listOf(4.0f, 8.0f, 12.5f, 17.5f),
        defaultIncrement = 4.0f
    ),
    BODYWEIGHT_CORE(
        displayName = "Bodyweight & Core",
        ladder = listOf(0.0f, 4.0f),
        defaultIncrement = 0.0f
    );

    fun getNextWeight(currentWeightKg: Float): Float {
        val nextInLadder = ladder.firstOrNull { it > currentWeightKg + 0.05f }
        return nextInLadder ?: (currentWeightKg + defaultIncrement)
    }
}

data class ExercisePrescription(
    val exerciseName: String,
    val category: MovementCategory,
    val week1RepRange: String,
    val week2RepRange: String,
    val week3RepRange: String,
    val week4TriggerRepRange: String,
    val week4TriggerMinReps: Int,
    val notes: String = ""
) {
    fun getTargetForWeek(week: Int): String {
        return when (week) {
            1 -> week1RepRange
            2 -> week2RepRange
            3 -> week3RepRange
            4 -> week4TriggerRepRange
            else -> week1RepRange
        }
    }
}

data class SegmentOverloadProfile(
    val segmentId: String,
    val primaryCategory: MovementCategory,
    val exercises: List<ExercisePrescription>
)

data class UpgradeEligibilityResult(
    val isEligible: Boolean,
    val triggerReason: String = "",
    val currentWeightKg: Float = 4.0f,
    val nextWeightKg: Float = 5.5f,
    val triggerRepsAchieved: Int = 0,
    val requiredMinReps: Int = 0,
    val w1RepRange: String = "",
    val segmentTitle: String = ""
)

object ProgressiveOverloadEngine {

    // 1. Hardcoded Exercise Catalog & 40s Rep Targets
    val SEGMENT_PROFILES: Map<String, SegmentOverloadProfile> = mapOf(
        // Monday: Chest
        "mon_2" to SegmentOverloadProfile(
            segmentId = "mon_2",
            primaryCategory = MovementCategory.LARGE_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Single-Arm Floor Press",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-15 reps/arm",
                    week4TriggerRepRange = "16-18 reps/arm",
                    week4TriggerMinReps = 16,
                    notes = "Unilateral press with elbow tucked 45°"
                ),
                ExercisePrescription(
                    exerciseName = "Single-Arm Floor Fly",
                    category = MovementCategory.SMALL_ISOLATION,
                    week1RepRange = "8-9 reps/arm",
                    week2RepRange = "10-11 reps/arm",
                    week3RepRange = "12-13 reps/arm",
                    week4TriggerRepRange = "14-15 reps/arm",
                    week4TriggerMinReps = 14,
                    notes = "Slight elbow bend, floor tap stretch"
                )
            )
        ),
        "mon_3" to SegmentOverloadProfile(
            segmentId = "mon_3",
            primaryCategory = MovementCategory.LARGE_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Two-Hand Dumbbell Pullover",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "9-10 reps",
                    week2RepRange = "11-12 reps",
                    week3RepRange = "13-14 reps",
                    week4TriggerRepRange = "15-16 reps",
                    week4TriggerMinReps = 15,
                    notes = "Deep lat and upper chest stretch"
                ),
                ExercisePrescription(
                    exerciseName = "Close-Grip Crush Press",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "10-11 reps",
                    week2RepRange = "12-13 reps",
                    week3RepRange = "14-15 reps",
                    week4TriggerRepRange = "16-18 reps",
                    week4TriggerMinReps = 16,
                    notes = "Squeeze dumbbell hard between both palms"
                )
            )
        ),
        "mon_4" to SegmentOverloadProfile(
            segmentId = "mon_4",
            primaryCategory = MovementCategory.BODYWEIGHT_CORE,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Uneven & Tap Push-Up Finisher (30s)",
                    category = MovementCategory.BODYWEIGHT_CORE,
                    week1RepRange = "6-8 reps",
                    week2RepRange = "8-10 reps",
                    week3RepRange = "10-12 reps",
                    week4TriggerRepRange = "13-15 reps",
                    week4TriggerMinReps = 13,
                    notes = "One hand on DB, switch sides or drop to knees"
                )
            )
        ),

        // Tuesday: Lower Body & Back
        "tue_2" to SegmentOverloadProfile(
            segmentId = "tue_2",
            primaryCategory = MovementCategory.LOWER_BODY,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Goblet Squat & RDL",
                    category = MovementCategory.LOWER_BODY,
                    week1RepRange = "10-12 reps",
                    week2RepRange = "12-14 reps",
                    week3RepRange = "15-16 reps (2s pause)",
                    week4TriggerRepRange = "18-20 reps",
                    week4TriggerMinReps = 18,
                    notes = "Chest upright on squat, hip hinge on RDL"
                ),
                ExercisePrescription(
                    exerciseName = "Split Lunge (Left & Right)",
                    category = MovementCategory.LOWER_BODY,
                    week1RepRange = "9-10 reps/leg",
                    week2RepRange = "11-12 reps/leg",
                    week3RepRange = "13-14 reps/leg",
                    week4TriggerRepRange = "15-16 reps/leg",
                    week4TriggerMinReps = 15,
                    notes = "Vertical front shin, tap rear knee gently"
                )
            )
        ),
        "tue_3" to SegmentOverloadProfile(
            segmentId = "tue_3",
            primaryCategory = MovementCategory.LARGE_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Split-Stance Single-Arm Row",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "10-12 reps/arm",
                    week2RepRange = "12-14 reps/arm",
                    week3RepRange = "15-16 reps/arm",
                    week4TriggerRepRange = "17-18 reps/arm",
                    week4TriggerMinReps = 17,
                    notes = "Drive elbow to hip pocket, 1s peak squeeze"
                )
            )
        ),
        "tue_4" to SegmentOverloadProfile(
            segmentId = "tue_4",
            primaryCategory = MovementCategory.BODYWEIGHT_CORE,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Torso Twists & Plank Pull-Throughs",
                    category = MovementCategory.BODYWEIGHT_CORE,
                    week1RepRange = "10-12 reps",
                    week2RepRange = "12-14 reps",
                    week3RepRange = "14-16 reps",
                    week4TriggerRepRange = "16-18 reps",
                    week4TriggerMinReps = 16,
                    notes = "Pull 4kg DB across body without hip twisting"
                )
            )
        ),

        // Wednesday: Dedicated Arms
        "wed_2" to SegmentOverloadProfile(
            segmentId = "wed_2",
            primaryCategory = MovementCategory.MEDIUM_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Supinating & Hammer Curls",
                    category = MovementCategory.MEDIUM_UPPER,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Rotate wrist fully at top, control eccentric descent"
                )
            )
        ),
        "wed_3" to SegmentOverloadProfile(
            segmentId = "wed_3",
            primaryCategory = MovementCategory.SMALL_ISOLATION,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Single-Arm Overhead Tricep Ext.",
                    category = MovementCategory.SMALL_ISOLATION,
                    week1RepRange = "8-10 reps/arm",
                    week2RepRange = "10-12 reps/arm",
                    week3RepRange = "12-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Keep elbow pointed up, stretch tricep long head"
                ),
                ExercisePrescription(
                    exerciseName = "Bent-Over Tricep Kickback",
                    category = MovementCategory.SMALL_ISOLATION,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Lock upper arm parallel to torso, peak squeeze"
                )
            )
        ),

        // Thursday: Lower Body Volume
        "thu_2" to SegmentOverloadProfile(
            segmentId = "thu_2",
            primaryCategory = MovementCategory.LOWER_BODY,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "2s Pause Squats & RDLs",
                    category = MovementCategory.LOWER_BODY,
                    week1RepRange = "10-12 reps",
                    week2RepRange = "12-14 reps",
                    week3RepRange = "15-16 reps (2s pause)",
                    week4TriggerRepRange = "18-20 reps",
                    week4TriggerMinReps = 18,
                    notes = "Full 2s pause at bottom of every squat"
                ),
                ExercisePrescription(
                    exerciseName = "Split Lunge (Left & Right)",
                    category = MovementCategory.LOWER_BODY,
                    week1RepRange = "9-10 reps/leg",
                    week2RepRange = "11-12 reps/leg",
                    week3RepRange = "13-14 reps/leg",
                    week4TriggerRepRange = "15-16 reps/leg",
                    week4TriggerMinReps = 15,
                    notes = "Strict tempo, no momentum off back foot"
                )
            )
        ),
        "thu_3" to SegmentOverloadProfile(
            segmentId = "thu_3",
            primaryCategory = MovementCategory.BODYWEIGHT_CORE,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Core Block",
                    category = MovementCategory.BODYWEIGHT_CORE,
                    week1RepRange = "10-12 reps",
                    week2RepRange = "12-14 reps",
                    week3RepRange = "14-16 reps",
                    week4TriggerRepRange = "16-18 reps",
                    week4TriggerMinReps = 16,
                    notes = "Anti-rotation plank holds and slow hollow pulses"
                )
            )
        ),
        "thu_4" to SegmentOverloadProfile(
            segmentId = "thu_4",
            primaryCategory = MovementCategory.BODYWEIGHT_CORE,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "3-Min Finisher: Snatches & Burpee Press",
                    category = MovementCategory.BODYWEIGHT_CORE,
                    week1RepRange = "8-10 reps",
                    week2RepRange = "10-12 reps",
                    week3RepRange = "12-14 reps",
                    week4TriggerRepRange = "15+ reps",
                    week4TriggerMinReps = 15,
                    notes = "Metabolic burn conditioning"
                )
            )
        ),

        // Friday: Chest & Shoulders
        "fri_1" to SegmentOverloadProfile(
            segmentId = "fri_1",
            primaryCategory = MovementCategory.LARGE_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Single-Arm Floor Press",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-15 reps/arm",
                    week4TriggerRepRange = "16-18 reps/arm",
                    week4TriggerMinReps = 16,
                    notes = "Unilateral floor press"
                ),
                ExercisePrescription(
                    exerciseName = "Single-Arm Floor Fly",
                    category = MovementCategory.SMALL_ISOLATION,
                    week1RepRange = "8-9 reps/arm",
                    week2RepRange = "10-11 reps/arm",
                    week3RepRange = "12-13 reps/arm",
                    week4TriggerRepRange = "14-15 reps/arm",
                    week4TriggerMinReps = 14,
                    notes = "Floor chest fly"
                ),
                ExercisePrescription(
                    exerciseName = "Two-Hand Dumbbell Pullover",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "9-10 reps",
                    week2RepRange = "11-12 reps",
                    week3RepRange = "13-14 reps",
                    week4TriggerRepRange = "15-16 reps",
                    week4TriggerMinReps = 15,
                    notes = "Pullover stretch"
                ),
                ExercisePrescription(
                    exerciseName = "Close-Grip Crush Press",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "10-11 reps",
                    week2RepRange = "12-13 reps",
                    week3RepRange = "14-15 reps",
                    week4TriggerRepRange = "16-18 reps",
                    week4TriggerMinReps = 16,
                    notes = "Crush press pump"
                )
            )
        ),
        "fri_2" to SegmentOverloadProfile(
            segmentId = "fri_2",
            primaryCategory = MovementCategory.MEDIUM_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Standing Single-Arm Shoulder Press",
                    category = MovementCategory.MEDIUM_UPPER,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Strict vertical press, core braced, no leg drive"
                )
            )
        ),

        // Saturday: Back Priority & Arms
        "sat_2" to SegmentOverloadProfile(
            segmentId = "sat_2",
            primaryCategory = MovementCategory.LARGE_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Split-Stance Single-Arm Row (4 sets)",
                    category = MovementCategory.LARGE_UPPER,
                    week1RepRange = "10-12 reps/arm",
                    week2RepRange = "12-14 reps/arm",
                    week3RepRange = "15-16 reps/arm",
                    week4TriggerRepRange = "17-18 reps/arm",
                    week4TriggerMinReps = 17,
                    notes = "Heavy volume row, stretch lats at bottom"
                )
            )
        ),
        "sat_3" to SegmentOverloadProfile(
            segmentId = "sat_3",
            primaryCategory = MovementCategory.MEDIUM_UPPER,
            exercises = listOf(
                ExercisePrescription(
                    exerciseName = "Supinating & Hammer Curls",
                    category = MovementCategory.MEDIUM_UPPER,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Biceps peak contraction"
                ),
                ExercisePrescription(
                    exerciseName = "Single-Arm Overhead Tricep Ext.",
                    category = MovementCategory.SMALL_ISOLATION,
                    week1RepRange = "8-10 reps/arm",
                    week2RepRange = "10-12 reps/arm",
                    week3RepRange = "12-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Tricep long head stretch"
                ),
                ExercisePrescription(
                    exerciseName = "Bent-Over Tricep Kickback",
                    category = MovementCategory.SMALL_ISOLATION,
                    week1RepRange = "9-10 reps/arm",
                    week2RepRange = "11-12 reps/arm",
                    week3RepRange = "13-14 reps/arm",
                    week4TriggerRepRange = "15-16 reps/arm",
                    week4TriggerMinReps = 15,
                    notes = "Tricep peak squeeze"
                )
            )
        )
    )

    fun getProfile(segmentId: String): SegmentOverloadProfile? {
        return SEGMENT_PROFILES[segmentId]
    }

    /**
     * Checks if a segment has qualified for a dumbbell weight ladder upgrade.
     *
     * Upgrade triggers on either:
     * - Condition A: Set 3 Reps >= week4TriggerMinReps AND effortRating != "Near Failure"
     * - Condition B: Last 3 consecutive sessions on this segment were rated "Easy"
     */
    fun checkUpgradeEligibility(
        segmentId: String,
        currentWeightKg: Float,
        set3Reps: Int,
        effortRating: String,
        recentLogs: List<DailyWorkoutLogEntity>
    ): UpgradeEligibilityResult {
        val profile = SEGMENT_PROFILES[segmentId] ?: return UpgradeEligibilityResult(
            isEligible = false,
            currentWeightKg = currentWeightKg,
            nextWeightKg = currentWeightKg
        )

        val primaryExercise = profile.exercises.firstOrNull() ?: return UpgradeEligibilityResult(
            isEligible = false,
            currentWeightKg = currentWeightKg,
            nextWeightKg = currentWeightKg
        )

        val triggerMinReps = primaryExercise.week4TriggerMinReps
        val nextWeightKg = profile.primaryCategory.getNextWeight(currentWeightKg)
        val w1RepRange = primaryExercise.week1RepRange

        // Condition A: Crushed Set 3 rep trigger with clean form (not Near Failure)
        val isConditionA = set3Reps >= triggerMinReps && !effortRating.equals("Near Failure", ignoreCase = true)

        // Condition B: Last 3 consecutive sessions on this segment logged as "Easy"
        val isConditionB = recentLogs.size >= 3 && recentLogs.take(3).all {
            it.effortRating.equals("Easy", ignoreCase = true)
        }

        if (isConditionA) {
            return UpgradeEligibilityResult(
                isEligible = true,
                triggerReason = "Condition A: Hit $set3Reps clean reps on Set 3 (Target: $triggerMinReps+ with '$effortRating')",
                currentWeightKg = currentWeightKg,
                nextWeightKg = nextWeightKg,
                triggerRepsAchieved = set3Reps,
                requiredMinReps = triggerMinReps,
                w1RepRange = w1RepRange,
                segmentTitle = primaryExercise.exerciseName
            )
        }

        if (isConditionB) {
            return UpgradeEligibilityResult(
                isEligible = true,
                triggerReason = "Condition B: 3 consecutive sessions logged with 'Easy' effort",
                currentWeightKg = currentWeightKg,
                nextWeightKg = nextWeightKg,
                triggerRepsAchieved = set3Reps,
                requiredMinReps = triggerMinReps,
                w1RepRange = w1RepRange,
                segmentTitle = primaryExercise.exerciseName
            )
        }

        return UpgradeEligibilityResult(
            isEligible = false,
            currentWeightKg = currentWeightKg,
            nextWeightKg = nextWeightKg,
            triggerRepsAchieved = set3Reps,
            requiredMinReps = triggerMinReps,
            w1RepRange = w1RepRange,
            segmentTitle = primaryExercise.exerciseName
        )
    }
}
