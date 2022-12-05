package mckts

import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.nio.file.Paths
import java.security.ProtectionDomain
import java.util.jar.JarFile
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.outputStream

@Suppress("unused")
object Agent {
  @JvmStatic
  fun premain(
    args: String?,
    instrumentation: Instrumentation,
  ) {
    instrumentation.appendToSystemClassLoaderSearch(getOrUnpackBrigadier())
    instrumentation.addTransformer(object : ClassFileTransformer {
      override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray,
      ): ByteArray? {
        return null // TODO
      }
    })
  }

  private fun getOrUnpackBrigadier(): JarFile {
    return JarFile("server.jar").use { server ->
      server
        .getInputStream(server.getEntry("META-INF/libraries.list"))
        .bufferedReader()
        .use { libraries ->
          val brigadierPath = libraries
            .readLines()
            .map { it.split('\t')[2] }
            .first { it.startsWith("com/mojang/brigadier/") }

          val outputPath = Paths
            .get("libraries", brigadierPath)
            .also { it.parent.createDirectories() }

          if (outputPath.notExists()) {
            server
              .getInputStream(server.getEntry("META-INF/libraries/$brigadierPath"))
              .buffered()
              .use { brigadier ->
                outputPath
                  .outputStream()
                  .buffered()
                  .use { output -> brigadier.transferTo(output) }
              }
          }

          JarFile(outputPath.toFile())
        }
    }
  }
}
