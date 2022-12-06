package mckts

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
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
      literal("kts:eval")
        .then(
          literal("code").then(
            argument("code", greedyString())
              .executes {
                evalCode(dispatcher, it.source, it["code"])
              }
          )
        )
        .then(
          literal("file").then(
            argument("file", greedyString())
              .executes {
                evalFile(dispatcher, it.source, it["file"])
              }
          )
        )
    )
  }

  private fun evalCode(
    dispatcher: CommandDispatcher<Any>,
    source: Any,
    code: String,
  ): Int {
    return evalSourceCode(dispatcher, source, code.toScriptSource())
  }

  private fun evalFile(
    dispatcher: CommandDispatcher<Any>,
    source: Any,
    file: String,
  ): Int {
    return evalSourceCode(dispatcher, source, File(file).toScriptSource())
  }

  private fun evalSourceCode(
    dispatcher: CommandDispatcher<Any>,
    source: Any,
    code: SourceCode,
  ): Int {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<McScript>()
    val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<McScript> {
      implicitReceivers(CommandExecutor(dispatcher, source))
    }

    val result = BasicJvmScriptingHost().eval(code, compilationConfiguration, evaluationConfiguration)

    result.reports.forEach {
      if (it.severity > ScriptDiagnostic.Severity.WARNING) {
        System.err.println(it.message)
      }
    }

    return 0
  }
}
