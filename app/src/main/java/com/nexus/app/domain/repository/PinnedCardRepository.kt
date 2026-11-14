package com.nexus.app.domain.repository

import com.nexus.app.domain.model.PinnedCardConfig

/**
 * The featured card on the feed (id pinned_featured_event_1). All suspend functions throw `ApiException` (no Result wrapper).
 */
interface PinnedCardRepository {
    /** GET /rest/v1/pinned_cards?id=eq.pinned_featured_event_1; null when missing, disabled, expired or not targeted at the user. */
    suspend fun loadPinnedCard(): PinnedCardConfig?

    /** POST /rest/v1/rpc/vote_on_poll {p_user_id,p_option_id}; returns the updated votes (userId to optionId). */
    suspend fun voteOnPinnedPoll(optionId: String): Map<String, String>

    /** POST /rest/v1/rpc/toggle_pinned_card_participation {p_user_id}; returns the updated participant ids. */
    suspend fun togglePinnedParticipation(): List<String>
}
