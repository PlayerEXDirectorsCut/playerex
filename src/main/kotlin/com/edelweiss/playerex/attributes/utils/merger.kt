package com.edelweiss.playerex.attributes.utils

typealias MergeableJSONMapping<T> = MutableMap<String, MutableMap<String, T>>

inline infix fun <reified T> MergeableJSONMapping<T>.merge(other: MergeableJSONMapping<T>) {
    this.keys.forEach { key ->
        val types = other.getOrDefault(key, mutableMapOf())
        this[key]?.forEach(types::put)
        other[key] = types
    }
}