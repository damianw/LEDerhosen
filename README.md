LEDerhosen
==========

LEDerhosen is a project to turn a NeoPixel RGBW LED strip into an audio spectrum visualization. I will update this
with some photos/videos once the physical hardware looks a little bit better.

This repo contains the desktop app, meant to be paired with a microcontroller running the
[LEDerhosen-Firmware](https://github.com/damianw/LEDerhosen-Firmware).

### Building & Running
There are two main "apps" housed in the project.
- The "server" runs on a machine which is directly connected to the microcontroller by the USB serial port. It receives
  OSC messages for the heights of the columns.
- The "client" runs on the machine with audio input. It does the bulk of the audio processing and sends it over to
  the server. The client and server can be run on the same machine, if you like.

To build a single fat binary:
```sh
./gradlew clean assemble
```

The binary will be located at `/build/LEDerhosen`.

```
Usage: LEDerhosen [options] [command] [command options]
  Options:
    --help, -h
       Prints usage information
       Default: false
  Commands:
    client      Runs the LEDerhosen client
      Usage: client [options]
        Options:
          --big-endian, -b
             Whether audio input is big endian
             Default: false
          --buffer-overlap, -o
             Audio buffer overlap
             Default: 3072
          --buffer-size, -u
             Audio buffer size
             Default: 4096
          --channels, -n
             Number of channels in audio
             Default: 1
          --columns, -c
             Number of columns (bars) in the spectrum
             Default: 16
        * --host, -h
             Server host to transmit messages to
        * --input, -i
             Name of audio input mixer
          --list-inputs, -l
             List available audio inputs
             Default: false
          --max-amplitude, -a
             Maximum amplitude for bar scaling
             Default: 400.0
          --port, -p
             Server port
             Default: 1605
          --sample-rate, -r
             Audio sample rate
             Default: 44100.0
          --sample-size, -s
             Audio sample size (in bits)
             Default: 16
          --signed, -x
             Whether audio input is signed
             Default: true

    server      Runs the LEDerhosen server
      Usage: server [options]
        Options:
          --columns, -c
             Number of columns (bars) in the spectrum
             Default: 16
          --list-serial-ports, -l
             List available serial ports
             Default: false
          --osc-port, -p
             Port to run the OSC server on
             Default: 1605
          --rows, -r
             Number of rows in the spectrum
             Default: 15
        * --serial-port, -s
             Device serial port
```

### FAQ

#### Why is it called `LEDerhosen`?
Turns out that there aren't that many words that start with `LED`.
