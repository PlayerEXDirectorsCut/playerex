package com.edelweiss.playerex.attributes.overrides

import com.edelweiss.skillattributes.enums.StackingFormula

/**
 * An interface meant for `EntityAttribute` to implement in a mixin.
 *
 * This allows for access provided by the attributes system.
 * */
interface PEXEntityAttribute {
    /** The minimum value of the attribute. */
    fun min(): Double

    /** The maximum value of the attribute. */
    fun max(): Double

    /** The attributes' stacking formula. */
    fun formula(): StackingFormula

    /** Immutable map of the parents attached to the attribute. */
    fun parents(): Map<PEXEntityAttribute, AttributeFunction>

    /** Immutable map of the children attached to the attribute. */
    fun children(): Map<PEXEntityAttribute, AttributeFunction>

    /** Immutable collection of the property keys attached to the attribute. */
    fun properties(): Collection<String>

    /** Checks whether this attribute has the input property key. */
    fun hasProperty(property: String): Boolean

    /** Tries to get the attributes property value assigned to the input property key. */
    fun getProperty(): String
}