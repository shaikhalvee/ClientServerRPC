import java.io.*;
import java.net.Socket;

public class MultiThreadedTCPClient {

	private static final String[] fileList = {"my_image.jpg", "my_image2.jpg", "my_image3.jpg", "my_image4.jpg",
			"my_image5.jpg"};
	final static String IMAGE_FOLDER = Constants.FilePath.CLIENT_IMAGE_FOLDER;
	final static String SERVER_IP = Constants.IP.LOCALHOST;
	final static int PORT = Constants.Ports.SERVER_PORT;
	final static int BUFFER_SIZE = Constants.Config.BUFFER_SIZE;

	public static void main(String[] args) {
		for (String fileName : fileList) {
			// Each request in a separate thread
			new Thread(() -> fetchFile(fileName)).start();
		}
	}

	private static void fetchFile(String fileName) {
		try (Socket socket = new Socket(SERVER_IP, PORT)) {
			PrintWriter outWriter = new PrintWriter(socket.getOutputStream(), true);
			outWriter.println(fileName);  // request

			BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = inputFromServer.readLine();
			if (line.startsWith("ERROR")) {
				System.err.println("[Client] " + line);
				return;
			}

			// Parse renamed filename
			String renamedFile = line.split(":", 2)[1];
			// Parse: "FILESIZE:<size>"
			line = inputFromServer.readLine();
			long fileSize = Long.parseLong(line.split(":", 2)[1]);

			File outputFile = new File(IMAGE_FOLDER, renamedFile);
			FileOutputStream fos = new FileOutputStream(outputFile);
			InputStream socketInputFromServer = socket.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			long totalRead = 0;
			int bytesRead;
			while (totalRead < fileSize && (bytesRead = socketInputFromServer.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
				totalRead += bytesRead;
			}
			fos.flush();
			System.out.println("[Client] Downloaded " + fileName + " as " + renamedFile +
					" in thread: " + Thread.currentThread().getName());
			socketInputFromServer.close();
			outWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
