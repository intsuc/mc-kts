package mckts

import kotlinx.coroutines.runBlocking
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@Suppress("unused")
@KotlinScript(
  fileExtension = "mc.kts",
  compilationConfiguration = McScriptCompilationConfiguration::class,
  evaluationConfiguration = McScriptEvaluationConfiguration::class,
)
abstract class McScript

object McScriptCompilationConfiguration : ScriptCompilationConfiguration(
  {
    defaultImports(DependsOn::class, Repository::class)

    jvm {
      dependenciesFromCurrentContext(
        "mc-kts",
        "kotlin-scripting-dependencies",
      )
    }

    refineConfiguration {
      onAnnotations(DependsOn::class, Repository::class) { context ->
        val annotations = context.collectedData
                            ?.get(ScriptCollectedData.collectedAnnotations)
                            ?.takeIf { it.isNotEmpty() }
                          ?: return@onAnnotations context.compilationConfiguration.asSuccess()
        runBlocking {
          McScriptCompilationConfiguration.resolver.resolveFromScriptSourceAnnotations(annotations)
        }.onSuccess {
          context.compilationConfiguration
            .with { dependencies.append(JvmDependency(it)) }
            .asSuccess()
        }
      }
    }
  }
) {
  private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())
}

object McScriptEvaluationConfiguration : ScriptEvaluationConfiguration(
  {}
)
