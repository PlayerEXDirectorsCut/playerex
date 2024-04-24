package com.edelweiss.playerex.attributes.mutable

import com.edelweiss.playerex.attributes.json.AttributeFunctionJSON
import com.edelweiss.playerex.attributes.json.AttributeOverrideJSON
import com.edelweiss.playerex.attributes.overrides.PEXEntityAttribute
import com.edelweiss.skillattributes.enums.StackingFormula

/** An interface for a mutable PlayerEX Entity Attribute. */
interface MutableEntityAttribute : PEXEntityAttribute {
    /** Overrides properties of the `PEXEntityAttribute`. */
    fun override(json: AttributeOverrideJSON)

    /** Sets additional properties for the `EntityAttribute` */
    fun setProperties(properties: Map<String, String>)

    /** Adds a parent `EntityAttribute` with an associated function. */
    fun addParent(parent: MutableEntityAttribute, function: AttributeFunctionJSON)

    /** Adds a child `EntityAttribute` with an associated function. */
    fun addChild(child: MutableEntityAttribute, function: AttributeFunctionJSON)

    /** Clears properties & relationships of the `EntityAttribute`. */
    fun clear()

    /** Procures the sum of values using specific parameters. */
    fun sum(v: Double, v2: Double, k: Double, k2: Double): Double

    /** Checks if a `EntityAttribute` is contained with another. */
    fun contains(lhs: MutableEntityAttribute, rhs: MutableEntityAttribute)

    /** Provides a mutable map of parent `EntityAttributes` with functions. */
    fun parentsMutable(): MutableMap<PEXEntityAttribute, AttributeFunctionJSON>

    /** Provides a mutable map of child `EntityAttributes` with functions. */
    fun childrenMutable(): MutableMap<PEXEntityAttribute, AttributeFunctionJSON>
}