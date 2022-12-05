package mckts

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext

@Suppress("NOTHING_TO_INLINE")
inline fun literal(
  name: String,
): LiteralArgumentBuilder<Any> =
  LiteralArgumentBuilder.literal(name)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> argument(
  name: String,
  type: ArgumentType<T>,
): RequiredArgumentBuilder<Any, T> =
  RequiredArgumentBuilder.argument(name, type)

inline operator fun <reified V : Any> CommandContext<Any>.get(
  name: String,
): V =
  getArgument(name, V::class.java)
