package com.edelweiss.playerex.attributes.json

import kotlinx.serialization.Serializable

@Serializable
data class EntityTypesJSON(val values: MutableMap<String, MutableMap<String, Double>>)