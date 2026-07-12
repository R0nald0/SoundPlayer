package com.example.soundplayer.service

import com.example.soundplayer.model.Sound
import javax.inject.Inject

class PlaylistOrderingService
    @Inject
    constructor() {
        fun sort(
            sounds: List<Sound>,
            orderType: Int,
        ): List<Sound> =
            when (orderType) {
                ORDER_BY_TITLE_ASC -> sounds.sortedBy { it.title }
                ORDER_BY_TITLE_DESC -> sounds.sortedByDescending { it.title }
                ORDER_BY_INSERTED_DATE -> sounds.sortedBy { it.insertedDate }
                else -> sounds
            }

        private companion object {
            const val ORDER_BY_TITLE_ASC = 0
            const val ORDER_BY_TITLE_DESC = 1
            const val ORDER_BY_INSERTED_DATE = 2
        }
    }
