package com.edelweiss.playerex.attributes.json

import kotlinx.serialization.Serializable

@Serializable
data class PropertiesJSON(val values: MutableMap<String, MutableMap<String, String>>)