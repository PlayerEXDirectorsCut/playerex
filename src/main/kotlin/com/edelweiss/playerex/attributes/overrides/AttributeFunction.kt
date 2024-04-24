package com.edelweiss.playerex.attributes.overrides

import com.edelweiss.skillattributes.enums.FunctionBehavior

interface AttributeFunction {
    /** Behavior associated with this function. */
    fun behavior(): FunctionBehavior

    /** The value provided by the function. */
    fun value(): Double
}