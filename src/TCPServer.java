import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

	final static int BUFFER_SIZE = 4096;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(Constants.Ports.SERVER_PORT)) {
			System.out.println("[Server] TCP Server started on port " + Constants.Ports.SERVER_PORT + "...");

			while (true) {
				// Accept one client (single-threaded)
				Socket clientSocket = serverSocket.accept();
				System.out.println("[Server] Accepted a client: " + clientSocket.getRemoteSocketAddress() + "with inet address: " + clientSocket.getInetAddress());

				// Create input/output streams
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);

				// Read a request line from the client
				String clientInput = inputReader.readLine();
				System.out.println("[Server] Client request: " + clientInput);

				// "Rename" logic: for demonstration, we’ll just append
				// the client’s remote port to the file name
				String renamedFile = appendInfoToFileName(clientInput, clientSocket.getPort());

				// Send the image
				File file = new File(Constants.FilePath.SERVER_IMAGE_FOLDER, clientInput);
				if (!file.exists()) {
					// Simple error handling: notify client
					outputWriter.println("ERROR: File not found");
					clientSocket.close();
					continue;
				}

				sendFile(file, renamedFile, clientSocket);

				clientSocket.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sendFile(File file, String renamedFile, Socket clientSocket) {
		try {
			// First send the "renamed" filename
			PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			outputWriter.println("FILENAME:" + renamedFile);

			// Then send file size
			long fileSize = file.length();
			outputWriter.println("FILESIZE:" + fileSize);

			// Send file contents (binary)
			BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file));
			OutputStream socketOut = clientSocket.getOutputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = fileIn.read(buffer)) != -1) {
				socketOut.write(buffer, 0, bytesRead);
			}

			socketOut.flush();
			fileIn.close();
			System.out.println("[Server] File sent to client with renamed: " + renamedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String appendInfoToFileName(String requestedFile, int port) {
		int dotIndex = requestedFile.lastIndexOf('.');
		if (dotIndex == -1) {
			return requestedFile + "_" + Constants.Modules.TCP + "_" + port;
		} else {
			String name = requestedFile.substring(0, dotIndex);
			String ext = requestedFile.substring(dotIndex);
			return name + "_" + Constants.Modules.TCP + "_" + port + ext;
		}
	}
}
