package mckts

import com.mojang.brigadier.CommandDispatcher

class CommandExecutor(
  private val dispatcher: CommandDispatcher<Any>,
  private val source: Any,
) {
  @Suppress("unused")
  fun execute(input: String): Int =
    dispatcher.execute(input, source)
}
