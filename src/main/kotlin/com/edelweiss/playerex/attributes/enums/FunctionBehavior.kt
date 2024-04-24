package com.edelweiss.skillattributes.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FunctionBehavior(val id: Byte) {
    @SerialName("ADD")
    Add(0),
    @SerialName("MULTIPLY")
    Multiply(1);

    override fun toString() = this.id.toString()
}