import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
	static final int BUFFER_SIZE = Constants.Config.BUFFER_SIZE;
	static final int UDP_SERVER_PORT = Constants.Ports.SERVER_PORT;
	static final String UDP_SERVER_IP = Constants.IP.LOCALHOST;
	static String fileName = "my_image.jpg";

	public static void main(String[] args) {
		try (DatagramSocket socket = new DatagramSocket()) {
			// Send filename to the UDP Server
			byte[] sendData = fileName.getBytes();
			InetAddress serverAddress = InetAddress.getByName(UDP_SERVER_IP);
			DatagramPacket requestPacket = new DatagramPacket(sendData, sendData.length, serverAddress, UDP_SERVER_PORT);
			socket.send(requestPacket);

			// Receive header: "FILENAME:xxx:FILESIZE:yyy"
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket headerPacket = new DatagramPacket(buffer, buffer.length);
			socket.receive(headerPacket);

			String header = new String(headerPacket.getData(), 0, headerPacket.getLength());
			if (header.startsWith("ERROR")) {
				System.err.println("[Client] " + header);
				return;
			}
			// Parse
			// e.g. "FILENAME:picture_12345.jpg:FILESIZE:12500"
			String[] parts = header.split(":");
			String renamed = parts[1];
			long fileSize = Long.parseLong(parts[3]);

			// Start receiving file data
			File outputFile = new File(Constants.FilePath.CLIENT_IMAGE_FOLDER, renamed);
			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
			long totalReceived = 0;

			while (totalReceived < fileSize) {
				DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
				socket.receive(dataPacket);

				// Write data to file
				fileOutputStream.write(dataPacket.getData(), 0, dataPacket.getLength());
				totalReceived += dataPacket.getLength();
			}

			fileOutputStream.close();
			System.out.println("[Client] File received and saved as: " + renamed);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
