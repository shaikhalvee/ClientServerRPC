import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MultiThreadPoolUDPServer {

	public static final int SERVER_PORT = Constants.Ports.SERVER_PORT;
	public static final int BUFFER_SIZE = Constants.Config.BUFFER_SIZE;
	public static final String IMAGE_FOLDER = Constants.FilePath.SERVER_IMAGE_FOLDER;

	public static void main(String[] args) {
		try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
			System.out.println("[Server] Multi-Threaded UDP Server on port " + SERVER_PORT);

			// Continuously listen for incoming packets
			while (true) {
				// 1) Receive request packet
				byte[] receiveBuffer = new byte[BUFFER_SIZE];
				DatagramPacket requestPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				socket.receive(requestPacket);

				// 2) Spawn a new thread to handle the request
				new Thread(() -> handleRequest(socket, requestPacket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleRequest(DatagramSocket socket, DatagramPacket requestPacket) {
		// Extract client info
		InetAddress clientAddr = requestPacket.getAddress();
		int clientPort = requestPacket.getPort();

		// Extract the requested file name from the packet
		String requestedFileName = new String(requestPacket.getData(), 0, requestPacket.getLength()).trim();
		System.out.println("[UDP Server] Client requests: " + requestedFileName);

		File file = new File(IMAGE_FOLDER, requestedFileName);
		if (!file.exists()) {
			String errorMsg = "ERROR: File not found";
			sendUDP(socket, errorMsg.getBytes(), clientAddr, clientPort);
			return;
		}

		String renamedFileName = appendInfoToFileName(requestedFileName, clientPort);

		long fileSize = file.length();
		// Construct header with both filename and filesize
		String header = "FILENAME:" + renamedFileName + " FILESIZE:" + fileSize;
		sendUDP(socket, header.getBytes(), clientAddr, clientPort);

		// Send file contents in chunks
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				DatagramPacket dataPacket = new DatagramPacket(buffer, bytesRead, clientAddr, clientPort);
				socket.send(dataPacket);
			}
			System.out.println("[UDP Server] File sent: " + renamedFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sendUDP(DatagramSocket socket, byte[] data, InetAddress addr, int port) {
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String appendInfoToFileName(String filename, int port) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			return filename + "_" + Constants.Modules.MUDP + "_" + port;
		} else {
			String name = filename.substring(0, dotIndex);
			String ext = filename.substring(dotIndex);
			return name + "_" + Constants.Modules.MUDP + "_" + port + ext;
		}
	}
}
