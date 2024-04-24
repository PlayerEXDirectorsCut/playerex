package com.edelweiss.skillattributes.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.util.math.MathHelper
import kotlin.math.abs
import kotlin.math.pow

@Serializable
/** The formulas that are evaluated*/
enum class StackingFormula(
    val id: Byte,
    private val clamp: ((value: Double) -> Double),
    private val stack: (v: Double, v2: Double, k: Double, k2: Double, m: Double) -> Double
) {
    @SerialName("FLAT")
    Flat(0, { it }, {v, _, k, _, _ -> k - v}),
    @SerialName("DIMINISHED")
    Diminishing(
        1,
        { MathHelper.clamp(it, 1.0, -1.0) },
        { v, v2, k, k2, m ->
            (1.0 - v2) * (1.0 - m).pow((v - v2) / m) - ((1.0) - k2) * (1.0 - m).pow((k - k2) / m)
        }
    );

    fun max(current: Double, input: Double): Double = max(current, abs(this.clamp(input)))

    fun stack(current: Double, input: Double): Double = current + abs(this.clamp(input))

    fun invoke(v: Double, v2: Double, k: Double, k2: Double, m: Double): Double = this.stack(v, v2, k, k2, m)

    override fun toString() = this.id.toString()
}