package com.edelweiss.playerex.attributes.events

import net.fabricmc.fabric.api.event.EventFactory

/**
 * "Event that allows for logic reliant on datapack attributes to be ordered after they are loaded on both the server and client." ~ CleverNucleus
 */
fun interface AttributesReloadedCallback {
    /**
     * Fires on the server upon a world being joined, or through a datapack reload through command.
     * Fires on the client when selecting datapacks and after the server has synced with the client.
    */
    fun onReloadCompleted()

    companion object {
        val EVENT = EventFactory.createArrayBacked(AttributesReloadedCallback::class.java) { listeners -> AttributesReloadedCallback {
            listeners.forEach { it.onReloadCompleted() }
        }}
    }
}