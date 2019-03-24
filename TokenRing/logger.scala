import java.io._
import java.net._


object Logger extends App {
  val buffer = new Array[Byte](1024)
  val socket = new MulticastSocket(9009)
  val writer = new BufferedWriter(new FileWriter(args(0)))
  socket.joinGroup(InetAddress.getByName("229.0.0.0"))
  while (true) {
    val packet = new DatagramPacket(buffer, buffer.length)
    socket.receive(packet)
    val msg = new String(packet.getData, "UTF-8").trim
    println(msg)
    writer.write(msg)
    writer.write("\n")
    writer.flush()
  }
}
