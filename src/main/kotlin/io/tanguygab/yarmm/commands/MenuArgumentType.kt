package io.tanguygab.yarmm.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import io.tanguygab.yarmm.YARMM
import io.tanguygab.yarmm.inventory.MenuInventory
import java.util.concurrent.CompletableFuture

class MenuArgumentType(val plugin: YARMM) : CustomArgumentType.Converted<MenuInventory, String> {
    override fun convert(nativeType: String) = plugin.menuManager.menus[nativeType]!!

    override fun getNativeType() = StringArgumentType.word()!!

    override fun <S: Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        plugin.menuManager.menus.keys
            .filter { it.startsWith(builder.remainingLowerCase) }
            .forEach { builder.suggest(it) }
        return builder.buildFuture()
    }
}