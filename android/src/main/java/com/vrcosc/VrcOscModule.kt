package com.vrcosc

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableType
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@ReactModule(name = VrcOscModule.NAME)
class VrcOscModule(reactContext: ReactApplicationContext) :
  NativeVrcOscSpec(reactContext) {

  private var clientAddress: String? = null
  private var clientPort: Int = 0
  private val executor = Executors.newSingleThreadExecutor()

  private var serverSocket: DatagramSocket? = null
  @Volatile
  private var isServerRunning = false
  private val serverExecutor = Executors.newSingleThreadExecutor()

  override fun getName(): String {
    return NAME
  }

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun createClient(address: String, port: Double) {
    clientAddress = address
    clientPort = port.toInt()
    Log.d(NAME, "createClient: $address:$clientPort")
  }

  private fun stopServer() {
    isServerRunning = false
    try {
      serverSocket?.close()
    } catch (e: Exception) {
      Log.e(NAME, "Error closing server socket: ${e.message}")
    }
    serverSocket = null
  }

  private fun decodeOsc(bytes: ByteArray, length: Int): Pair<String, List<Any>>? {
    var offset = 0

    fun readString(): String? {
      if (offset >= length) return null
      val start = offset
      while (offset < length && bytes[offset] != 0.toByte()) {
        offset++
      }
      if (offset >= length) return null
      val strBytes = bytes.copyOfRange(start, offset)
      offset++ // Skip null terminator
      val padding = (4 - (offset % 4)) % 4
      offset += padding
      return String(strBytes, Charsets.UTF_8)
    }

    val address = readString() ?: return null
    if (!address.startsWith("/")) return null

    val typeTag = readString() ?: return null
    if (!typeTag.startsWith(",")) return null

    val args = mutableListOf<Any>()
    for (i in 1 until typeTag.length) {
      val tag = typeTag[i]
      when (tag) {
        'i' -> {
          if (offset + 4 > length) break
          val value = ByteBuffer.wrap(bytes, offset, 4).int
          args.add(value)
          offset += 4
        }
        'f' -> {
          if (offset + 4 > length) break
          val value = ByteBuffer.wrap(bytes, offset, 4).float
          args.add(value)
          offset += 4
        }
        's' -> {
          val str = readString() ?: break
          args.add(str)
        }
        'T' -> {
          args.add(true)
        }
        'F' -> {
          args.add(false)
        }
      }
    }
    return Pair(address, args)
  }

  override fun createServer(address: String, port: Double) {
    val portInt = port.toInt()
    Log.d(NAME, "createServer: $address:$portInt")
    stopServer()

    if (portInt > 0) {
      try {
        serverSocket = if (address.isEmpty() || address == "0.0.0.0") {
          DatagramSocket(portInt)
        } else {
          DatagramSocket(portInt, InetAddress.getByName(address))
        }
        isServerRunning = true

        serverExecutor.execute {
          val buffer = ByteArray(2048)
          while (isServerRunning) {
            try {
              val socket = serverSocket ?: break
              val packet = DatagramPacket(buffer, buffer.size)
              socket.receive(packet)

              val result = decodeOsc(packet.data, packet.length)
              if (result != null) {
                val (addr, args) = result
                val params = Arguments.createMap().apply {
                  putString("address", addr)
                  putArray("data", Arguments.createArray().apply {
                    for (arg in args) {
                      when (arg) {
                        is Int -> pushInt(arg)
                        is Float -> pushDouble(arg.toDouble())
                        is Double -> pushDouble(arg)
                        is Boolean -> pushBoolean(arg)
                        is String -> pushString(arg)
                      }
                    }
                  })
                }
                reactApplicationContext
                  .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                  .emit("GotMessage", params)
              }
            } catch (e: Exception) {
              if (isServerRunning) {
                Log.e(NAME, "Error receiving packet: ${e.message}")
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.e(NAME, "Error starting OSC server: ${e.message}")
      }
    }
  }

  override fun sendMessage(address: String, data: ReadableArray) {
    val host = clientAddress
    val port = clientPort
    if (host == null || port == 0) {
      Log.e(NAME, "Client not initialized. Call createClient first.")
      return
    }

    executor.execute {
      try {
        val oscData = encodeOsc(address, data)
        val socket = DatagramSocket()
        val inetAddress = InetAddress.getByName(host)
        val packet = DatagramPacket(oscData, oscData.size, inetAddress, port)
        socket.send(packet)
        socket.close()
        Log.d(NAME, "OSC message sent to $host:$port$address")
      } catch (e: Exception) {
        Log.e(NAME, "Error sending OSC message: ${e.message}")
      }
    }
  }

  private fun encodeOsc(address: String, data: ReadableArray): ByteArray {
    val bos = ByteArrayOutputStream()
    
    // 1. Address Pattern
    writePaddedString(bos, address)
    
    // 2. Type Tags
    val tags = StringBuilder(",")
    for (i in 0 until data.size()) {
      when (data.getType(i)) {
        ReadableType.String -> tags.append("s")
        ReadableType.Number -> {
           val v = data.getDouble(i)
           if (v == v.toInt().toDouble()) tags.append("i") else tags.append("f")
        }
        ReadableType.Boolean -> {
          tags.append(if (data.getBoolean(i)) "T" else "F")
        }
        else -> tags.append("?")
      }
    }
    writePaddedString(bos, tags.toString())
    
    // 3. Arguments
    for (i in 0 until data.size()) {
      when (data.getType(i)) {
        ReadableType.String -> writePaddedString(bos, data.getString(i) ?: "")
        ReadableType.Number -> {
          val v = data.getDouble(i)
          if (v == v.toInt().toDouble()) {
            val b = ByteBuffer.allocate(4).putInt(v.toInt()).array()
            bos.write(b)
          } else {
            val b = ByteBuffer.allocate(4).putFloat(v.toFloat()).array()
            bos.write(b)
          }
        }
        // Booleans T/F have no data in payload
        else -> {}
      }
    }
    
    return bos.toByteArray()
  }

  private fun writePaddedString(bos: ByteArrayOutputStream, s: String) {
    val b = s.toByteArray(Charsets.UTF_8)
    bos.write(b)
    bos.write(0) // Null terminator
    var pad = 4 - (b.size + 1) % 4
    if (pad == 4) pad = 0
    for (i in 0 until pad) {
      bos.write(0)
    }
  }

  override fun addListener(eventType: String) { }

  override fun removeListeners(count: Double) { }

  override fun invalidate() {
    stopServer()
    executor.shutdown()
    serverExecutor.shutdown()
    super.invalidate()
  }

  companion object {
    const val NAME = "VrcOsc"
  }
}
