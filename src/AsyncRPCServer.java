import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AsyncRPCServer - Asynchronous RPC example
 * <p>
 * Protocol:
 *  - "REQUEST: foo <iterations>" -> returns "RESPONSE: <rpcId>" immediately,
 *       then does 'foo' in the background.
 *  - "REQUEST: add <i> <j>" -> same idea.
 *  - "REQUEST: sort <list of int>" -> same idea.
 *  - "REQUEST: getResult <rpcId>" -> returns "RESPONSE: <result>" if ready,
 *       or "RESPONSE: NOT_READY" if not yet computed.
 * </p>
 */
public class AsyncRPCServer {
	public static final int PORT = Constants.Ports.RPC_PORT;

	// A thread-safe map from rpcId -> result string
	private static final ConcurrentHashMap<Integer, String> rpcIDHashMap = new ConcurrentHashMap<>();

	// Generate unique RPC IDs
	private static final AtomicInteger requestIdGenerator = new AtomicInteger(Constants.Config.RPC_ID);

	public static void main(String[] args) {
		System.out.println("[AsyncRPCServer] Listening on port " + PORT);
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("[Server] Accepted connection from " + clientSocket.getInetAddress()
						+ ":" + clientSocket.getPort());
				// handle each client in a separate thread
				new Thread(() -> handleClient(clientSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleClient(Socket socket) {
		try (Socket clientSocket = socket;
		     BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		     PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {

			String request = inputReader.readLine();
			if (request == null || !request.startsWith("REQUEST:")) {
				outputWriter.println("RESPONSE: ERROR - Invalid request");
				return;
			}

			// e.g. "REQUEST: foo 100000"
			String[] parts = request.substring("REQUEST:".length()).trim().split("\\s+");
			String method = parts[0];

			switch (method) {
				case "foo": {
					// "foo <iterations>"
					int iterations = Integer.parseInt(parts[1]);
					int rpcId = requestIdGenerator.getAndIncrement();
					// Immediately respond with the rpcId
					outputWriter.println("RESPONSE: " + rpcId);

					// Asynchronously compute foo
					new Thread(() -> {
						long dummy = foo(iterations);
						// Store the result
						rpcIDHashMap.put(rpcId, "OK: foo=" + dummy);
					}).start();

					break;
				}
				case "add": {
					// "add <i> <j>"
					int i = Integer.parseInt(parts[1]);
					int j = Integer.parseInt(parts[2]);
					int rpcId = requestIdGenerator.getAndIncrement();
					outputWriter.println("RESPONSE: " + rpcId);

					// Asynchronously compute add
					new Thread(() -> {
						int sum = add(i, j);
						rpcIDHashMap.put(rpcId, String.valueOf(sum));
					}).start();

					break;
				}
				case "sort": {
					// "sort 5 9 1 3 2"
					int[] arr = new int[parts.length - 1];
					for (int k = 1; k < parts.length; k++) {
						arr[k - 1] = Integer.parseInt(parts[k]);
					}
					int rpcId = requestIdGenerator.getAndIncrement();
					outputWriter.println("RESPONSE: " + rpcId);

					// Asynchronously sort
					new Thread(() -> {
						Arrays.sort(arr);
						rpcIDHashMap.put(rpcId, Arrays.toString(arr));
					}).start();

					break;
				}
				case "getResult": {
					// "getResult <rpcId>"
					int queryId = Integer.parseInt(parts[1]);
					String result = rpcIDHashMap.get(queryId);
					if (result == null) {
						outputWriter.println("RESPONSE: NOT_READY");
					} else {
						outputWriter.println("RESPONSE: " + result);
					}
					break;
				}
				default:
					outputWriter.println("RESPONSE: ERROR - Unknown method");
			}

		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	// Simulated "foo" that returns a dummy sum for demonstration
	private static long foo(int iterations) {
		long sum = 0;
		for (int i = 0; i < iterations; i++) {
			sum += i;
		}
		return sum;
	}

	private static int add(int i, int j) {
		return i + j;
	}
}
