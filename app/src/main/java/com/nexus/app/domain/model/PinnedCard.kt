package com.nexus.app.domain.model

data class PollOption(val id: String, val text: String)

/** Row of `pinned_cards` (`type` is event/participation/poll/survey; `theme` defaults to "royal"). */
data class PinnedCardConfig(
    val id: String,
    val enabled: Boolean,
    val type: String,
    val theme: String,
    val badgeText: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val endDate: String?,
    val actionButtonText: String?,
    val actionButtonUrl: String?,
    val targetYear: Int?,
    val targetMajor: String?,
    val pollOptions: List<PollOption>,
    /** userId -> optionId. */
    val votes: Map<String, String>,
    val participants: List<String>
) {
    val isPoll: Boolean get() = type.equals("poll", true) || type.equals("survey", true)
    val isParticipation: Boolean get() = type.equals("participation", true) || type.equals("event", true)
}
