package wtf.log.lederhosen.cli

import com.fazecast.jSerialComm.SerialPort
import wtf.log.lederhosen.server.OscServer
import wtf.log.lederhosen.server.SpectrumController

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/24/17
 * (C) 2017
 */
object ServerMain {

  fun run(serverArguments: ServerArguments): Unit = with(serverArguments) {
    if (listPorts) listPorts()
    else runServer(columnCount, rowCount, serialPort, oscPort)
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

}
