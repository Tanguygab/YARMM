package io.tanguygab.yarmm.config.menu.meta

import com.google.gson.JsonObject
import io.tanguygab.yarmm.YARMM
import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Bukkit
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.util.UUID
import kotlin.io.encoding.Base64

class SkullMetaConfig(val head: String) : ItemMetaConfig(SkullMeta::class) {
    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "head" to property(item, player, head)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        meta as SkullMeta

        val head = data["head"]!!
        if (head.update() || force) {
            val skin = TAB.getInstance().configuration.skinManager.getSkin(head.get().trim())
            if (skin == null) {
                meta.playerProfile = null
                return
            }

            if (meta.playerProfile == null)
                meta.playerProfile = Bukkit.createProfile(UUID.randomUUID())

            val textures = YARMM.gson.fromJson(Base64.decode(skin.value).toString(Charsets.UTF_8), JsonObject::class.java)
            val url = textures["textures"]?.asJsonObject["SKIN"]?.asJsonObject["url"]?.asString

            val profile = meta.playerProfile!!
            profile.setTextures(profile.textures.apply {
                this.skin = if (url == null) null else URI.create(url).toURL()
            })

            // blocking when skin not found, should find an alternative soon
            meta.playerProfile = profile
        }
    }
}