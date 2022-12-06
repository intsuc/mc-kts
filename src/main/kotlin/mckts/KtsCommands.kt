package mckts

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.implicitReceivers
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
          eval(dispatcher, it.source, it["file"])
        }
      )
    )
  }

  private fun eval(
    dispatcher: CommandDispatcher<Any>,
    source: Any,
    file: String,
  ): Int {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<McScript> {
      implicitReceivers(CommandExecutor::class)
    }
    val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<McScript> {
      implicitReceivers(CommandExecutor(dispatcher, source))
    }

    val result = BasicJvmScriptingHost().eval(File(file).toScriptSource(), compilationConfiguration, evaluationConfiguration)

    result.reports.forEach {
      if (it.severity > ScriptDiagnostic.Severity.WARNING) {
        System.err.println(it.message)
      }
    }

    return 0
  }
}
