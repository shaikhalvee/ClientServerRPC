import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SyncRPCServer {

	private static final int PORT = Constants.Ports.RPC_PORT;

	public static void main(String[] args) {
		System.out.println("[Server] Starting RPCServer on port " + PORT);
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				// Accept a new client
				Socket clientSocket = serverSocket.accept();
				System.out.println("[Server] Accepted connection from " + clientSocket.getInetAddress()
						+ ":" + clientSocket.getPort());

				// Handle each client connection in a separate thread
				new Thread(() -> handleClient(clientSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleClient(Socket clientSocket) {
		try (Socket socket = clientSocket;
		     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

			// Read exactly one request line
			String request = in.readLine();
			if (request == null || !request.startsWith("REQUEST:")) {
				out.println("RESPONSE: ERROR - Invalid request");
				return;
			}

			// Example: "REQUEST: foo 100000"
			//          "REQUEST: add 3 5"
			//          "REQUEST: sort 5 9 1 3 2"
			String[] parts = request.substring("REQUEST:".length()).trim().split("\\s+");
			String methodName = parts[0];

			String response;
			switch (methodName) {
				case "foo": {
					// e.g. "foo 100000"
					int iterations = Integer.parseInt(parts[1]);
					foo(iterations); // do some heavy lifting
					response = "OK";
					break;
				}
				case "add": {
					// e.g. "add 3 5"
					int i = Integer.parseInt(parts[1]);
					int j = Integer.parseInt(parts[2]);
					int sum = add(i, j);
					response = String.valueOf(sum); // "8"
					break;
				}
				case "sort": {
					// e.g. "sort 5 9 1 3 2"
					int[] array = new int[parts.length - 1];
					for (int k = 1; k < parts.length; k++) {
						array[k - 1] = Integer.parseInt(parts[k]);
					}
					sort(array); // sort in-place
					response = Arrays.toString(array); // "[1, 2, 3, 5, 9]"
					break;
				}
				default:
					response = "ERROR - Unknown method";
			}

			out.println("RESPONSE: " + response);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Simulate a CPU-intensive method
	private static void foo(int iterations) {
		long dummy = 0;
		for (int i = 0; i < iterations; i++) {
			dummy *= i; // do some dummy work
		}
	}

	private static int add(int i, int j) {
		return i + j;
	}

	private static void sort(int[] arr) {
		Arrays.sort(arr);
	}
}
