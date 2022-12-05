package mckts

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader
import java.nio.file.Paths

object Fork {
  @JvmStatic
  fun main(args: Array<String>) {
    val classLoader = URLClassLoader(arrayOf(
      Paths
        .get("server.jar")
        .toUri()
        .toURL())
    )
    val mainClass = Class.forName("net.minecraft.bundler.Main", true, classLoader)
    val mainHandle = MethodHandles
      .lookup()
      .findStatic(mainClass, "main", MethodType.methodType(Void.TYPE, Array<String>::class.java))
      .asFixedArity()
    mainHandle(args as Any)
  }
}
