package video.api.rtmpdroid

import android.os.ParcelFileDescriptor
import video.api.rtmpdroid.amf.AmfEncoder
import video.api.rtmpdroid.amf.models.NullParameter
import video.api.rtmpdroid.amf.models.ObjectParameter
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class RtmpServer {
    private val executor = Executors.newCachedThreadPool()
    private val serverSocket = ServerSocket(0)

    val port: Int = serverSocket.localPort

    private fun sendConnectResult(rtmp: Rtmp, transactionId: Int) {
        val amfEncoder = AmfEncoder().apply {
            add("_result")
            add(transactionId.toDouble())
            // Information
            val objectParameter = ObjectParameter()
            objectParameter.add("level", "status")
            objectParameter.add("code", "NetConnection.Connect.Success")
            objectParameter.add("description", "Connection succeeded.")
            add(objectParameter)
        }
        val body = amfEncoder.encode()
        val packet = RtmpPacket(0x03, 1, PacketType.COMMAND, 0, body)
        rtmp.writePacket(packet)
    }

    private fun sendResultNumber(rtmp: Rtmp, transactionId: Int, streamId: Int) {
        val amfEncoder = AmfEncoder().apply {
            add("_result")
            add(transactionId.toDouble())
            add(NullParameter())
            add(streamId.toDouble())
        }
        val body = amfEncoder.encode()
        val packet = RtmpPacket(0x03, 1, PacketType.COMMAND, 0, body)
        rtmp.writePacket(packet)
    }

    private fun sendOnStatus(rtmp: Rtmp, transactionId: Int) {
        val amfEncoder = AmfEncoder().apply {
            add("onStatus")
            add(0.0)
            add(NullParameter())
            // Information
            val objectParameter = ObjectParameter()
            objectParameter.add("level", "status")
            objectParameter.add("code", "NetStream.Publish.Start")
            objectParameter.add("description", "Publish started.")
            add(objectParameter)
        }
        val body = amfEncoder.encode()
        val packet = RtmpPacket(0x03, 1, PacketType.COMMAND, 0, body)
        rtmp.writePacket(packet)
    }

    private fun invokeServer(rtmp: Rtmp, fd: Int) {
        rtmp.serve(fd)
        var packet = rtmp.readPacket() // connect
        sendConnectResult(rtmp, 1)
        packet = rtmp.readPacket() // releaseStream
        packet = rtmp.readPacket() // FCPublish
        packet = rtmp.readPacket() // createStream
        sendResultNumber(rtmp, 4, 1) // createStream - result
        packet = rtmp.readPacket() // publish
        sendOnStatus(rtmp, 5)
    }

    fun enqueueConnect(): Future<Boolean> {
        return executor.submit(Callable {
            val clientSocket = serverSocket.accept()
            Rtmp().use {
                invokeServer(it, ParcelFileDescriptor.fromSocket(clientSocket).detachFd())
            }
            true
        })
    }

    fun enqueueRead(): Future<ByteBuffer> {
        return executor.submit(Callable {
            val clientSocket = serverSocket.accept()
            Rtmp().use {
                invokeServer(it, ParcelFileDescriptor.fromSocket(clientSocket).detachFd())
                val packet = it.readPacket()
                packet.buffer
            }
        })
    }

    fun shutdown() {
        serverSocket.close()
        executor.shutdown()
    }
}