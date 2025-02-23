import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadedTCPServer {

	final static int PORT = Constants.Ports.SERVER_PORT;
	final static String IMAGE_FOLDER = Constants.FilePath.SERVER_IMAGE_FOLDER;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("[Server] Multi-Threaded TCP Server on port " + PORT + "...");
			while (true) {
				// 1. Block until a new client connects
				Socket clientSocket = serverSocket.accept();
				System.out.println("[Server] Accepted a client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

				// 2. Spawn a thread to handle this client
				new Thread(() -> handleClient(clientSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleClient(Socket clientSocket) {
		try (Socket socket = clientSocket) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String requestedFile = inputReader.readLine();
			System.out.println("[Worker] Received: " + requestedFile);

			String renamedFile = appendPortToFileName(requestedFile, socket.getPort());

			File file = new File(IMAGE_FOLDER, requestedFile);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			if (!file.exists()) {
				out.println("ERROR: File not found");
				return;
			}

			// Send file
			out.println("FILENAME:" + renamedFile);
			out.println("FILESIZE:" + file.length());

			try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file));
			     OutputStream socketOut = socket.getOutputStream()) {

				byte[] buffer = new byte[Constants.Config.BUFFER_SIZE];
				int bytesRead;
				while ((bytesRead = fileIn.read(buffer)) != -1) {
					socketOut.write(buffer, 0, bytesRead);
				}
				socketOut.flush();
			}
			System.out.println("[Worker] Finished client request in thread: " + Thread.currentThread().getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String appendPortToFileName(String filename, int port) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			return filename + "_" + Constants.Modules.MTCP + "_" + port;
		} else {
			String name = filename.substring(0, dotIndex);
			String ext = filename.substring(dotIndex);
			return name + "_" + Constants.Modules.MTCP + "_" + port + ext;
		}
	}
}
