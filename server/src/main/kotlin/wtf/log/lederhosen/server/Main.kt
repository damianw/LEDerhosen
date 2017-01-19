package wtf.log.lederhosen.server

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.fazecast.jSerialComm.SerialPort
import kotlin.system.exitProcess

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/27/16
 * (C) 2016
 *
 * The OSC Server
 */

/**
 * Command line arguments
 */
private object ProgramArguments {

  @Parameter(
      names = arrayOf("--help", "-h"),
      description = "Prints usage information",
      help = true
  )
  var help: Boolean = false

  class SerialPortConverter : IStringConverter<SerialPort> {
    override fun convert(value: String): SerialPort {
      val port = SerialPort.getCommPort(value)!!
      try {
        if (!port.openPort()) {
          throw ParameterException("Unknown serial port: $value")
        }
      } finally {
        port.closePort()
      }
      return port
    }
  }

  @Parameter(
      names = arrayOf("--serial-port", "-s"),
      description = "Device serial port",
      converter = SerialPortConverter::class,
      required = true
  )
  lateinit var serialPort: SerialPort

  @Parameter(
      names = arrayOf("--list-serial-ports", "-l"),
      description = "List available serial ports",
      help = true
  )
  var listPorts: Boolean = false

  @Parameter(
      names = arrayOf("--columns", "-c"),
      description = "Number of columns",
      required = true
  )
  var columnCount: Int? = null

  @Parameter(
      names = arrayOf("--rows", "-r"),
      description = "Number of rows",
      required = true
  )
  var rowCount: Int? = null

  @Parameter(
      names = arrayOf("--osc-port", "-p"),
      description = "Port to run the OSC server on"
  )
  var oscPort: Int = 1605

}

fun main(args: Array<String>) {
  val commander = JCommander(ProgramArguments)

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

  ProgramArguments.apply {
    when {
      help -> exitWithUsage()
      listPorts -> listPorts()
      else -> runServer(columnCount!!, rowCount!!, serialPort, oscPort)
    }
  }
}

private fun listPorts() {
  SerialPort.getCommPorts()
      .map(SerialPort::getSystemPortName)
      .forEach(::println)
}

private fun runServer(columnCount: Int, rowCount: Int, serialPort: SerialPort, oscPort: Int) {
  val controller = SpectrumController.open(columnCount, rowCount, serialPort)
  OscServer(controller, oscPort).apply(OscServer::start)
  KeepAliveThread().apply(Thread::start)
  println("OSC server for ${serialPort.systemPortName} running on port $oscPort")
}

/**
 * Dummy thread to keep the app alive since the OSC server is a daemon
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
private class KeepAliveThread : Thread("Osc-KeepAlive") {

  override fun run() {
    val monitor = Any()
    try {
      synchronized(monitor) {
        (monitor as java.lang.Object).wait()
      }
    } catch (ignored: InterruptedException) {
    }
  }

}
