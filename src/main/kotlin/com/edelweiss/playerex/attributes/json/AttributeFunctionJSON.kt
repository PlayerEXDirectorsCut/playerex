package com.edelweiss.playerex.attributes.json

import com.edelweiss.skillattributes.enums.FunctionBehavior
import com.edelweiss.skillattributes.utils.find
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer

@Serializable
data class AttributeFunctionJSON(
    /** The behavior associated with the attribute function. */
    @SerialName("behaviour")
    val behavior: FunctionBehavior,
    /** The value associated with this attribute function. */
    val value: Double
)
{
    companion object {
        /**
         * Attempts to read the data through a byte array.
         * This is prone to fail, and will return null if it does.
         * */
        fun read(array: ByteArray): AttributeFunctionJSON? {
            val behavior = (FunctionBehavior::id find array[8]) ?: return null
            return AttributeFunctionJSON(behavior, ByteBuffer.wrap(array).getDouble())
        }
    }

    /**
     * Writes to a byte array of a fixed size (9).
     * The identifier for the `FunctionBehavior` is set at the last index.
     */
    fun write(): ByteArray {
        val array = ByteArray(9)
        ByteBuffer.wrap(array).putDouble(this.value)
        return array
    }
}