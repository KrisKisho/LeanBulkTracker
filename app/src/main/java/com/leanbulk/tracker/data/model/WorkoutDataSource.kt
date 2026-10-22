package com.leanbulk.tracker.data.model

import java.time.DayOfWeek

object WorkoutDataSource {

    const val VIDEO_CHEST = "l5yuivJYj5k"
    const val VIDEO_ARMS = "mcuFTrNDLkg"
    const val VIDEO_FULL_BODY = "udbU6ykFFGE"

    fun getWeeklyPlans(): Map<DayOfWeek, DayWorkoutPlan> = mapOf(
        DayOfWeek.MONDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.MONDAY,
            title = "Chest & Push Hypertrophy",
            subtitle = "Pectoral mass, pressing power & dumbbell control",
            focusDescription = "Target chest hypertrophy using single 4kg dumbbell & bodyweight mechanical drop sets.",
            segments = listOf(
                WorkoutSegment(
                    id = "mon_1",
                    dayOfWeek = DayOfWeek.MONDAY,
                    orderIndex = 1,
                    title = "Chest Warm-up",
                    videoId = VIDEO_CHEST,
                    startSeconds = 20f,
                    endSeconds = 223f,
                    note = "Shoulder blades activation, arm circles & light dynamic stretching"
                ),
                WorkoutSegment(
                    id = "mon_2",
                    dayOfWeek = DayOfWeek.MONDAY,
                    orderIndex = 2,
                    title = "Single-Arm Floor Press & Fly",
                    videoId = VIDEO_CHEST,
                    startSeconds = 225f,
                    endSeconds = 790f,
                    note = "Unilateral 4kg dumbbell floor press into isolated flyes with controlled 3s tempo"
                ),
                WorkoutSegment(
                    id = "mon_3",
                    dayOfWeek = DayOfWeek.MONDAY,
                    orderIndex = 3,
                    title = "Pullover, Wide & Crush Grip Press",
                    videoId = VIDEO_CHEST,
                    startSeconds = 817f,
                    endSeconds = 1515f,
                    note = "Squeeze the 4kg dumbbell with both palms for max inner-chest contraction"
                ),
                WorkoutSegment(
                    id = "mon_4",
                    dayOfWeek = DayOfWeek.MONDAY,
                    orderIndex = 4,
                    title = "Uneven & Tap Push-Up Finisher",
                    videoId = VIDEO_CHEST,
                    startSeconds = 1530f,
                    endSeconds = 1806f,
                    note = "Bodyweight overload push-up mechanical drop set to burn out remaining fibers"
                ),
                WorkoutSegment(
                    id = "mon_5",
                    dayOfWeek = DayOfWeek.MONDAY,
                    orderIndex = 5,
                    title = "Upper Body Cooldown",
                    videoId = VIDEO_CHEST,
                    startSeconds = 1820f,
                    endSeconds = 2030f,
                    note = "Chest openers, shoulder mobility and recovery breathing"
                )
            )
        ),
        DayOfWeek.TUESDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.TUESDAY,
            title = "Legs, Back & Core",
            subtitle = "Modular Full Body Dumbbell Circuit",
            focusDescription = "Lower body quad/posterior chain overload combined with 4 unilateral back rows.",
            segments = listOf(
                WorkoutSegment(
                    id = "tue_1",
                    dayOfWeek = DayOfWeek.TUESDAY,
                    orderIndex = 1,
                    title = "Full Body Warm-up",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 20f,
                    endSeconds = 210f,
                    note = "Hip openers, spinal twists & hamstring prep"
                ),
                WorkoutSegment(
                    id = "tue_2",
                    dayOfWeek = DayOfWeek.TUESDAY,
                    orderIndex = 2,
                    title = "Lower Body: Goblet Squat, RDL & Split Lunge",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 215f,
                    endSeconds = 682f,
                    note = "Hold 4kg DB at chest; slow descent to maximize quad and glute tension"
                ),
                WorkoutSegment(
                    id = "tue_3",
                    dayOfWeek = DayOfWeek.TUESDAY,
                    orderIndex = 3,
                    title = "Back Focus: Split-Stance Single-Arm Rows",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1077f,
                    endSeconds = 1270f,
                    note = "Repeat for 4 sets/arm with 4kg DB. (Skip Chest & Shoulder Press segments)"
                ),
                WorkoutSegment(
                    id = "tue_4",
                    dayOfWeek = DayOfWeek.TUESDAY,
                    orderIndex = 4,
                    title = "Core: Torso Twists & Plank Pull-Throughs",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1275f,
                    endSeconds = 1530f,
                    note = "Pull 4kg dumbbell across body during plank to build anti-rotation core strength"
                ),
                WorkoutSegment(
                    id = "tue_5",
                    dayOfWeek = DayOfWeek.TUESDAY,
                    orderIndex = 5,
                    title = "Full Body Cooldown Stretch",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1760f,
                    endSeconds = 1963f,
                    note = "Hamstrings, hip flexors, lats and spinal decompression"
                )
            )
        ),
        DayOfWeek.WEDNESDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.WEDNESDAY,
            title = "Dedicated Arms",
            subtitle = "Biceps Hypertrophy & Triceps Density",
            focusDescription = "Isolation volume for arm growth with 6.1\" wrist structure emphasis.",
            segments = listOf(
                WorkoutSegment(
                    id = "wed_1",
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    orderIndex = 1,
                    title = "Wrist & Shoulder Warm-up",
                    videoId = VIDEO_ARMS,
                    startSeconds = 15f,
                    endSeconds = 108f,
                    note = "Wrist flexion/extension & rotator cuff preparation"
                ),
                WorkoutSegment(
                    id = "wed_2",
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    orderIndex = 2,
                    title = "Biceps: Supinating, Hammer Curls & 90° Pulses",
                    videoId = VIDEO_ARMS,
                    startSeconds = 115f,
                    endSeconds = 695f,
                    note = "Controlled cadence: 3 seconds down, hard peak squeeze at top"
                ),
                WorkoutSegment(
                    id = "wed_3",
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    orderIndex = 3,
                    title = "Triceps: Overhead Extensions, Kickbacks & Pulses",
                    videoId = VIDEO_ARMS,
                    startSeconds = 718f,
                    endSeconds = 1295f,
                    note = "Single 4kg dumbbell overhead extension targeting long head and horseshoe triceps"
                ),
                WorkoutSegment(
                    id = "wed_4",
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    orderIndex = 4,
                    title = "Arm Stretch Cooldown",
                    videoId = VIDEO_ARMS,
                    startSeconds = 1305f,
                    endSeconds = 1415f,
                    note = "Biceps wall stretch, triceps overhead stretch and wrist extensors"
                )
            )
        ),
        DayOfWeek.THURSDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.THURSDAY,
            title = "Lower Body Volume & Conditioning",
            subtitle = "Quad Burn, Posterior Chain & Metabolic Finisher",
            focusDescription = "Time-under-tension pause squats, RDLs and a 3-minute high-intensity metabolic finisher.",
            segments = listOf(
                WorkoutSegment(
                    id = "thu_1",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    orderIndex = 1,
                    title = "Full Body Warm-up",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 20f,
                    endSeconds = 210f,
                    note = "Deep squat mobility, hip bridges and ankle prep"
                ),
                WorkoutSegment(
                    id = "thu_2",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    orderIndex = 2,
                    title = "Lower Body Volume: 2s Pause Squats, RDLs & Lunges",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 215f,
                    endSeconds = 682f,
                    note = "Add strict 2-second pause at bottom of each rep to maximize muscle tension"
                ),
                WorkoutSegment(
                    id = "thu_3",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    orderIndex = 3,
                    title = "Core Block",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1275f,
                    endSeconds = 1530f,
                    note = "Dynamic core stability and transverse abdominis activation"
                ),
                WorkoutSegment(
                    id = "thu_4",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    orderIndex = 4,
                    title = "3-Min Finisher: Snatches, Burpee Press & Jack Press",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1538f,
                    endSeconds = 1745f,
                    note = "Metabolic burn conditioning to stimulate nutrient partitioning towards muscle"
                ),
                WorkoutSegment(
                    id = "thu_5",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    orderIndex = 5,
                    title = "Full Body Cooldown Stretch",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1760f,
                    endSeconds = 1963f,
                    note = "Calves, quads, glutes and lower back decompression"
                )
            )
        ),
        DayOfWeek.FRIDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.FRIDAY,
            title = "Chest & Shoulders Push",
            subtitle = "Compound Upper Pushing Hypertrophy",
            focusDescription = "Complete chest session combined with bonus overhead shoulder presses for 3D deltoids.",
            segments = listOf(
                WorkoutSegment(
                    id = "fri_1",
                    dayOfWeek = DayOfWeek.FRIDAY,
                    orderIndex = 1,
                    title = "Full Chest Workout",
                    videoId = VIDEO_CHEST,
                    startSeconds = 20f,
                    endSeconds = 1806f,
                    note = "Comprehensive chest routine with presses, flies and bodyweight push variations"
                ),
                WorkoutSegment(
                    id = "fri_2",
                    dayOfWeek = DayOfWeek.FRIDAY,
                    orderIndex = 2,
                    title = "Bonus Standing Single-Arm Shoulder Press",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 892f,
                    endSeconds = 1075f,
                    note = "Strict overhead dumbbell press for anterior and lateral deltoid capping"
                ),
                WorkoutSegment(
                    id = "fri_3",
                    dayOfWeek = DayOfWeek.FRIDAY,
                    orderIndex = 3,
                    title = "Upper Body Cooldown",
                    videoId = VIDEO_CHEST,
                    startSeconds = 1820f,
                    endSeconds = 2030f,
                    note = "Chest and shoulder flexibility stretch"
                )
            )
        ),
        DayOfWeek.SATURDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.SATURDAY,
            title = "Back Thickness & Arms Pump",
            subtitle = "Pull Volume, Biceps Peak & Triceps",
            focusDescription = "Heavy back priority row sets paired with isolated arm volume.",
            segments = listOf(
                WorkoutSegment(
                    id = "sat_1",
                    dayOfWeek = DayOfWeek.SATURDAY,
                    orderIndex = 1,
                    title = "Wrist & Shoulder Warm-up",
                    videoId = VIDEO_ARMS,
                    startSeconds = 15f,
                    endSeconds = 108f,
                    note = "Joint lubrication and warm-up movements"
                ),
                WorkoutSegment(
                    id = "sat_2",
                    dayOfWeek = DayOfWeek.SATURDAY,
                    orderIndex = 2,
                    title = "Back Priority: Split-Stance Rows (4 sets/arm)",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1077f,
                    endSeconds = 1270f,
                    note = "4 sets per arm. Squeeze lat at peak, stretch under control at bottom"
                ),
                WorkoutSegment(
                    id = "sat_3",
                    dayOfWeek = DayOfWeek.SATURDAY,
                    orderIndex = 3,
                    title = "Biceps & Triceps Workout",
                    videoId = VIDEO_ARMS,
                    startSeconds = 115f,
                    endSeconds = 1415f,
                    note = "Complete super-pump arm session for maximal muscle swelling"
                )
            )
        ),
        DayOfWeek.SUNDAY to DayWorkoutPlan(
            dayOfWeek = DayOfWeek.SUNDAY,
            title = "Active Mobility, Core & Recovery",
            subtitle = "Restoration, Joint Health & Core Stability",
            focusDescription = "Promote muscle recovery, lower systemic inflammation and prepare for the next microcycle.",
            segments = listOf(
                WorkoutSegment(
                    id = "sun_1",
                    dayOfWeek = DayOfWeek.SUNDAY,
                    orderIndex = 1,
                    title = "Thoracic & Cat-Cow Mobility",
                    videoId = VIDEO_CHEST,
                    startSeconds = 20f,
                    endSeconds = 223f,
                    note = "Thoracic extension and scapular retraction"
                ),
                WorkoutSegment(
                    id = "sun_2",
                    dayOfWeek = DayOfWeek.SUNDAY,
                    orderIndex = 2,
                    title = "Climber Rotations & Bird-Dogs",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 84f,
                    endSeconds = 210f,
                    note = "Glute medius and contralateral back chain activation"
                ),
                WorkoutSegment(
                    id = "sun_3",
                    dayOfWeek = DayOfWeek.SUNDAY,
                    orderIndex = 3,
                    title = "Core Stabilization Block",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1275f,
                    endSeconds = 1530f,
                    note = "Isometric holds and rotational core strength"
                ),
                WorkoutSegment(
                    id = "sun_4",
                    dayOfWeek = DayOfWeek.SUNDAY,
                    orderIndex = 4,
                    title = "Deep Full Body Stretch",
                    videoId = VIDEO_FULL_BODY,
                    startSeconds = 1760f,
                    endSeconds = 1963f,
                    note = "Full body release, lower cortisol, deep nasal breathing"
                )
            )
        )
    )

    fun getPlanForDay(dayOfWeek: DayOfWeek): DayWorkoutPlan {
        return getWeeklyPlans()[dayOfWeek] ?: getWeeklyPlans()[DayOfWeek.MONDAY]!!
    }
}
