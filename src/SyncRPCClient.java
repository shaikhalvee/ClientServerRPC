import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

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
		int numberOfLoops = 1, iterations = 1000000000;
		if (args.length != 0) {
			numberOfLoops = Integer.parseInt(args[0]);
			iterations = Integer.parseInt(args[1]);
		}
		String serverHost = Constants.IP.CLIENT_IP;
		int serverPort = Constants.Ports.RPC_PORT;
		List<Long> durations = new ArrayList<>();
		SyncRPCClient client = new SyncRPCClient(serverHost, serverPort);

		long startTimeAll = System.currentTimeMillis();
		for (int i = 0; i < numberOfLoops; i++) {
			try {
				// Example calls
				long startTime = System.currentTimeMillis();
				client.handleFoo(iterations);
				int a = (int) (Math.random() * 100000);
				int b = (int) (Math.random() * 10000000);
				int sum = client.handleAdd(a, b);
				System.out.println("[Client Main] Sum from server: " + sum);
				int[] arr = IntStream.generate(() -> new Random().nextInt(100) + 500).limit(200).toArray();
				int[] sorted = client.handleSort(arr);
				long endTime = System.currentTimeMillis();
				durations.add(endTime - startTime);
				System.out.println("[Client Main] Sorted array from server: " + Arrays.toString(sorted));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long endTimeAll = System.currentTimeMillis();
		long totalTime = endTimeAll - startTimeAll;

		System.out.println("[Client Main] Completed " + numberOfLoops + " calls to all operations");
		System.out.println("[Client Main] Total time = " + totalTime + " ms");
		for (int i = 0; i < durations.size(); i++) {
			System.out.println("  Call " + (i + 1) + " took " + durations.get(i) + " ms");
		}
	}
}
