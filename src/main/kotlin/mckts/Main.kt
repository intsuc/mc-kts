package mckts

import java.nio.file.Paths

object Main {
  @JvmStatic
  fun main(args: Array<String>) {
    val java = ProcessHandle
      .current()
      .info()
      .command()
      .orElseThrow()
    val jar = Paths
      .get(Main::class.java.protectionDomain.codeSource.location.toURI())
      .toAbsolutePath()
      .toString()
    ProcessBuilder(java, "-javaagent:\"$jar\"", "-cp", jar, "mckts.Fork", *args)
      .inheritIO()
      .start()
      .waitFor()
  }
}
