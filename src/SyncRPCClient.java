import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class SyncRPCClient {
	private final String serverHost;
	private final int serverPort;

	public SyncRPCClient(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	// Simulate a synchronous remote call to foo(iterations)
	public void handleFoo(int iterations) throws IOException {
		String request = "REQUEST: foo " + iterations;
		String response = sendRequest(request);
		// For foo, we expect "RESPONSE: OK"
		System.out.println("[Client] foo(" + iterations + ") => " + response);
	}

	// Synchronous remote call to add(i, j) => returns sum
	public int handleAdd(int i, int j) throws IOException {
		String request = "REQUEST: add " + i + " " + j;
		String response = sendRequest(request);
		System.out.println("[Client] add(" + i + ", " + j + ") => " + response);
		// parse int
		return Integer.parseInt(response);
	}

	// Synchronous remote call to sort(array) => returns sorted array
	public int[] handleSort(int[] array) throws IOException {
		StringBuilder sb = new StringBuilder("REQUEST: sort");
		for (int val : array) {
			sb.append(" ").append(val);
		}
		String request = sb.toString();

		String response = sendRequest(request);
		System.out.println("[Client] sort(" + Arrays.toString(array) + ") => " + response);

		// parse something like "[1, 2, 3, 5, 9]"
		// quick hack: strip brackets, split by comma
		response = response.replace("[", "").replace("]", "");
		String[] parts = response.split(",");
		int[] sorted = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			sorted[i] = Integer.parseInt(parts[i].trim());
		}
		return sorted;
	}

	/**
	 * Helper method that:
	 * 1) Opens a socket
	 * 2) Sends the request line
	 * 3) Reads the "RESPONSE: ..." line
	 * 4) Returns the text after "RESPONSE: "
	 */
	private String sendRequest(String request) throws IOException {
		try (Socket socket = new Socket(serverHost, serverPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			out.println(request);
			String line = in.readLine();
			if (line == null) {
				throw new IOException("No response from server");
			}
			if (!line.startsWith("RESPONSE:")) {
				throw new IOException("Invalid response: " + line);
			}
			// e.g. "RESPONSE: OK" => "OK"
			return line.substring("RESPONSE:".length()).trim();
		}
	}

	// Testing
	public static void main(String[] args) {
		String serverHost = Constants.IP.CLIENT_IP;
		int serverPort = Constants.Ports.RPC_PORT;

		SyncRPCClient client = new SyncRPCClient(serverHost, serverPort);
		try {
			// Example calls
			client.handleFoo(1000000000);
			int sum = client.handleAdd(3332, 53434);
			System.out.println("[Client Main] Sum from server: " + sum);

			int[] arr = {5, 9, 1, 3, 2};
			int[] sorted = client.handleSort(arr);
			System.out.println("[Client Main] Sorted array from server: " + Arrays.toString(sorted));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
