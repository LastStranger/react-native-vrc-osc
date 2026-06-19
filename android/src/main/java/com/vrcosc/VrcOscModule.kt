package com.vrcosc

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableType
import com.facebook.react.module.annotations.ReactModule
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

  override fun createServer(address: String, port: Double) {
    Log.w(NAME, "createServer() not implemented on Android yet (address=$address, port=$port)")
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

  companion object {
    const val NAME = "VrcOsc"
  }
}

