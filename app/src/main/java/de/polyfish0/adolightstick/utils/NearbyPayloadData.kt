package de.polyfish0.adolightstick.utils

import kotlinx.serialization.Serializable

@Serializable
data class NearbyPayloadData(val eventId: String, val data: String)
