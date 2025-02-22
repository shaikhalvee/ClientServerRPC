import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

	static final int BUFFER_SIZE = Constants.Config.BUFFER_SIZE;
	static final int UDP_SERVER_PORT = Constants.Ports.SERVER_PORT;

	public static void main(String[] args) {
		try (DatagramSocket socket = new DatagramSocket(UDP_SERVER_PORT)) {
			System.out.println("[Server] UDP Server listening on port " + UDP_SERVER_PORT);
			byte[] receiveBuffer = new byte[BUFFER_SIZE];
			while (true) {
				// Single-threaded: handle one request at a time
				DatagramPacket requestPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				socket.receive(requestPacket);

				InetAddress clientAddress = requestPacket.getAddress();
				int clientPort = requestPacket.getPort();

				// Extract filename
				String requestedFile = new String(requestPacket.getData(), 0, requestPacket.getLength()).trim();

				File file = new File(Constants.FilePath.SERVER_IMAGE_FOLDER, requestedFile);
				if (!file.exists()) {
					// Send error
					String errorMsg = "ERROR: File not found";
					byte[] errorData = errorMsg.getBytes();
					DatagramPacket errorPacket = new DatagramPacket(errorData, errorData.length, clientAddress, clientPort);
					socket.send(errorPacket);
					continue;
				}

				// "Rename"
				String renamedFile = appendInfoToFileName(requestedFile, clientPort);

				// 1) Send renamed file name
				String header = "FILENAME:" + renamedFile + ":FILESIZE:" + file.length();
				byte[] headerData = header.getBytes();
				DatagramPacket headerPacket = new DatagramPacket(headerData, headerData.length,
						clientAddress, clientPort);
				socket.send(headerPacket);

				// 2) Send file contents
				try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
					byte[] sendBuffer = new byte[BUFFER_SIZE];
					int bytesRead;
					while ((bytesRead = bis.read(sendBuffer)) != -1) {
						DatagramPacket dataPacket = new DatagramPacket(
								sendBuffer, bytesRead, clientAddress, clientPort);
						socket.send(dataPacket);
					}
				}
				System.out.println("Sent file: " + requestedFile + " as " + renamedFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String appendInfoToFileName(String requestedFile, int port) {
		int dotIndex = requestedFile.lastIndexOf('.');
		if (dotIndex == -1) {
			return requestedFile + "_" + Constants.Modules.UDP + "_" + port;
		} else {
			String name = requestedFile.substring(0, dotIndex);
			String ext = requestedFile.substring(dotIndex);
			return name + "_" + Constants.Modules.UDP + "_" + port + ext;
		}
	}
}
