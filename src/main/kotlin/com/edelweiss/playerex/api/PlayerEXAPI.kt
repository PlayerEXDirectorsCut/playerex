package com.edelweiss.playerex.api

import com.edelweiss.playerex.PlayerEXDirectorsCut
import com.edelweiss.playerex.cache.PlayerEXCacheAPI
import com.edelweiss.playerex.values.LevelValue
import net.minecraft.util.Identifier

/** Singleton used for main API access for PlayerEX. */
object PlayerEXAPI {
    val LEVEL_VALUE = PlayerEXCacheAPI.register(LevelValue())


    /**
    * Creates and returns an `Identifier` based on the PlayerEX mod id.
    * */
    fun id(path: String) = Identifier(PlayerEXDirectorsCut.MODID, path)
}
