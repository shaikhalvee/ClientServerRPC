import java.io.*;
import java.net.Socket;

public class TCPClient {

	static String fileName = "my_image.jpg";

	public static void main(String[] args) {
		try (Socket socket = new Socket(Constants.IP.LOCALHOST, Constants.Ports.SERVER_PORT)) {
			PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
			outputWriter.println(fileName);

			// Read server response
			BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Expect "FILENAME:<renamed>" or "ERROR:..."
			String line = inputFromServer.readLine();
			if (line.startsWith("ERROR")) {
				System.out.println("[Client] " + line);
				return;
			}

			// Parse renamed filename
			String renamed = line.split(":", 2)[1];
			// Next line: "FILESIZE:<size>"
			line = inputFromServer.readLine();
			long fileSize = 0;
			if (line.startsWith("FILESIZE:")) {
				fileSize = Long.parseLong(line.split(":", 2)[1]);
			}

			// Receive file
			File outputFile = new File(Constants.FilePath.CLIENT_IMAGE_FOLDER, renamed);
			FileOutputStream fos = new FileOutputStream(outputFile);
			InputStream socketInputFromServer = socket.getInputStream();
			byte[] buffer = new byte[Constants.Config.BUFFER_SIZE];
			int totalRead = 0;
			int bytesRead;
			while (totalRead < fileSize && (bytesRead = socketInputFromServer.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
				totalRead += bytesRead;
			}
			fos.close();
			System.out.println("[Client] File received and saved as: " + outputFile.getName()
					+ " in path: " + outputFile.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
