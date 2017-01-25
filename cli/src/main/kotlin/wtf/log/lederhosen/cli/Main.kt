package wtf.log.lederhosen.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import kotlin.system.exitProcess

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/24/17
 * (C) 2017
 */
fun main(args: Array<String>) {
  val programArguments = ProgramArguments()
  val clientArguments = ClientArguments()
  val serverArguments = ServerArguments()
  val commander = JCommander(programArguments).apply {
    setProgramName("LEDerhosen")
    addCommand(clientArguments)
    addCommand(serverArguments)
  }

  fun exitWithUsage(message: String? = null): Nothing {
    message?.let { System.err.println(it) }
    commander.usage()
    exitProcess(1)
  }

  try {
    commander.parse(*args)
  } catch (e: Exception) {
    exitWithUsage(e.message)
  }

  if (programArguments.help) {
    exitWithUsage()
  }

  try {
    when (commander.parsedCommand) {
      ProgramArguments.COMMAND_CLIENT -> ClientMain.run(clientArguments)
      ProgramArguments.COMMAND_SERVER -> ServerMain.run(serverArguments)
      else -> exitWithUsage()
    }
  } catch (e: ParameterException) {
    exitWithUsage(e.message)
  }
}
