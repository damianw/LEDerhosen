package wtf.log.lederhosen.cli

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import com.fazecast.jSerialComm.SerialPort

/**
 * Server command-line arguments
 */
@Parameters(
    commandNames = arrayOf(ProgramArguments.COMMAND_SERVER),
    commandDescription = "Runs the LEDerhosen server"
)
class ServerArguments {

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
      description = "Number of columns (bars) in the spectrum"
  )
  var columnCount: Int = 16

  @Parameter(
      names = arrayOf("--rows", "-r"),
      description = "Number of rows in the spectrum"
  )
  var rowCount: Int = 15

  @Parameter(
      names = arrayOf("--osc-port", "-p"),
      description = "Port to run the OSC server on"
  )
  var oscPort: Int = 1605

}
