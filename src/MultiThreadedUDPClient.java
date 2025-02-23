import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MultiThreadedUDPClient {

	private static final int SERVER_PORT = Constants.Ports.SERVER_PORT;
	private final static String SERVER_IP = Constants.IP.LOCALHOST;
	private static final int BUFFER_SIZE = Constants.Config.BUFFER_SIZE;
	private static final String IMAGE_FOLDER = Constants.FilePath.CLIENT_IMAGE_FOLDER;
	private static final String[] fileList = {"my_image.jpg", "my_image2.jpg", "my_image3.jpg", "my_image4.jpg",
			"my_image5.jpg"};

	public static void main(String[] args) {
		for (String fileName : fileList) {
			new Thread(() -> downloadFile(fileName)).start();
		}
	}

	private static void downloadFile(String fileName) {
		try (DatagramSocket socket = new DatagramSocket()) {
			InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

			// Send the file name as request
			byte[] requestData = fileName.getBytes();
			DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddr, SERVER_PORT);
			socket.send(requestPacket);

			// Receive the header which should contain both filename and filesize
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket headerPacket = new DatagramPacket(buffer, buffer.length);
			socket.receive(headerPacket);

			String header = new String(headerPacket.getData(), 0, headerPacket.getLength()).trim();
			if (header.startsWith("ERROR")) {
				System.err.println("[UDP Client] " + header);
				return;
			}

			// Expected header format: "FILENAME:<filename> FILESIZE:<filesize>"
			String renamed = null;
			long fileSize = 0;
			String[] headerParts = header.split("\\s+");
			for (String part : headerParts) {
				if (part.startsWith("FILENAME:")) {
					renamed = part.substring("FILENAME:".length());
				} else if (part.startsWith("FILESIZE:")) {
					fileSize = Long.parseLong(part.substring("FILESIZE:".length()));
				}
			}

			if (renamed == null) {
				System.err.println("[UDP Client] Header parsing error: Filename missing.");
				return;
			}

			System.out.println("[UDP Client] Parsed header: Filename = " + renamed + ", Filesize = " + fileSize);

			// Prepare local file for saving
			File outFile = new File(IMAGE_FOLDER, renamed);
			try (FileOutputStream fos = new FileOutputStream(outFile)) {
				long totalReceived = 0;
				while (totalReceived < fileSize) {
					DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
					socket.receive(dataPacket);
					fos.write(dataPacket.getData(), 0, dataPacket.getLength());
					totalReceived += dataPacket.getLength();
				}
				fos.flush();
				System.out.println("[UDP Client] Downloaded " + renamed + " (" + totalReceived +
						" bytes) to " + outFile.getAbsolutePath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
