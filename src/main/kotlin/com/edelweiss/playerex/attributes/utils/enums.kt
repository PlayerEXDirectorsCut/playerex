package com.edelweiss.skillattributes.utils

inline infix fun <reified E : Enum<E>, V> ((E) -> V).find(id: V): E? {
    return enumValues<E>().firstOrNull { this(it) == id }
}