package mckts

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.EXPAND_FRAMES
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
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
    instrumentation.addTransformer(Transformer)
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

  object Transformer : ClassFileTransformer {
    override fun transform(
      loader: ClassLoader?,
      className: String,
      classBeingRedefined: Class<*>?,
      protectionDomain: ProtectionDomain?,
      classfileBuffer: ByteArray,
    ): ByteArray? {
      return when (className) {
        "net/minecraft/server/MinecraftServer"   -> transform(classfileBuffer) { modNameTransformer(it) }
        "com/mojang/brigadier/CommandDispatcher" -> transform(classfileBuffer) { commandInjector(it) }
        else                                     -> null
      }
    }
  }

  private inline fun transform(
    classfileBuffer: ByteArray,
    create: (ClassVisitor) -> ClassVisitor,
  ): ByteArray {
    val classReader = ClassReader(classfileBuffer)
    val classWriter = ClassWriter(classReader, 0)
    val classVisitor = create(classWriter);
    classReader.accept(classVisitor, EXPAND_FRAMES)
    return classWriter.toByteArray()
  }

  private fun modNameTransformer(
    classVisitor: ClassVisitor,
  ): ClassVisitor {
    return object : ClassVisitor(ASM9, classVisitor) {
      override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
      ): MethodVisitor {
        val parent = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if (name == "getServerModName" && descriptor == "()Ljava/lang/String;") {
          object : MethodVisitor(ASM9, parent) {
            override fun visitLdcInsn(value: Any?) {
              super.visitLdcInsn("mc-kts")
            }
          }
        } else {
          parent
        }
      }
    }
  }

  private fun commandInjector(
    classVisitor: ClassVisitor,
  ): ClassVisitor {
    return object : ClassVisitor(ASM9, classVisitor) {
      override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
      ): MethodVisitor {
        val parent = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if (name == "<init>" && descriptor == "(Lcom/mojang/brigadier/tree/RootCommandNode;)V") {
          object : MethodVisitor(ASM9, parent) {
            override fun visitInsn(opcode: Int) {
              if (opcode == RETURN) {
                visitVarInsn(ALOAD, 0)
                visitMethodInsn(INVOKESTATIC, "mckts/KtsCommands", "register", "(Lcom/mojang/brigadier/CommandDispatcher;)V", false)
              }
              super.visitInsn(opcode)
            }
          }
        } else {
          parent
        }
      }
    }
  }
}
