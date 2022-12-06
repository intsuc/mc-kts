@file:Suppress("unused")

package mckts.core

import mckts.CommandExecutor

fun CommandExecutor.reloadPacks(): Int =
  execute("reload")
