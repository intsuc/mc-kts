package mckts

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

@Suppress("unused")
object KtsCommands {
  @JvmStatic
  fun register(
    dispatcher: CommandDispatcher<Any>,
  ) {
    dispatcher.register(
      literal("kts:eval").then(
        argument("file", greedyString()).executes {
          eval(it["file"])
        }
      )
    )
  }

  private fun eval(file: String): Int {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<McScript>()
    val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<McScript>()
    val result = BasicJvmScriptingHost().eval(file.toScriptSource(), compilationConfiguration, evaluationConfiguration)

    result.reports.forEach {
      if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
        println(it.message)
      }
    }

    return 0
  }
}
