package wtf.log.lederhosen.cli

import com.beust.jcommander.Parameter

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/24/17
 * (C) 2017
 */
class ProgramArguments {

  @Parameter(
      names = arrayOf("--help", "-h"),
      description = "Prints usage information",
      help = true
  )
  var help: Boolean = false

  companion object {

    const val COMMAND_CLIENT = "client"
    const val COMMAND_SERVER = "server"

  }

}
