package com.example.streaks.Model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

data class streakData(
    val streakId : Int,

    val streakName: String,

    val color: Color,

    val frequency : Frequency,

    val startDate: LocalDate ,

    val endDate: LocalDate ,

    val count: Int
)
enum class Frequency{
    DAILY , WEEKLY , MONTHLY
}


