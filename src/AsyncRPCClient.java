import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * AsyncRPCClient - Asynchronous RPC client
 * <p>
 * 1) asyncFoo(iterations) -> returns an rpcId
 * 2) asyncAdd(i, j)       -> returns an rpcId
 * 3) asyncSort(array)     -> returns an rpcId
 * 4) getResult(rpcId)     -> returns the result or "NOT_READY"
 * </p>
 */
public class AsyncRPCClient {
	private final String serverHost;
	private final int serverPort;

	public AsyncRPCClient(String host, int port) {
		this.serverHost = host;
		this.serverPort = port;
	}

	public int handleAsyncFoo(int iterations) throws IOException {
		String request = "REQUEST: foo " + iterations;
		String response = sendRequest(request);
		// response should be "RESPONSE: <rpcId>"
		return parseRpcId(response);
	}

	public int handleAsyncAdd(int i, int j) throws IOException {
		String request = "REQUEST: add " + i + " " + j;
		String response = sendRequest(request);
		return parseRpcId(response);
	}

	public int handleAsyncSort(int[] array) throws IOException {
		StringBuilder sb = new StringBuilder("REQUEST: sort");
		for (int val : array) {
			sb.append(" ").append(val);
		}
		String response = sendRequest(sb.toString());
		return parseRpcId(response);
	}

	/**
	 * Poll for the result. If the result is not ready, returns "NOT_READY".
	 * Otherwise, returns the actual result string (e.g. "OK: foo=..." or "8" or "[1, 2, 3]").
	 */
	public String getResult(int rpcId) throws IOException {
		String request = "REQUEST: getResult " + rpcId;
		String response = sendRequest(request);
		// response might be "RESPONSE: NOT_READY" or "RESPONSE: <some result>"
		return parseResponse(response);
	}

	/**
	 * Low-level helper to open a socket, send a request line, and read one response line.
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
			return line; // e.g. "RESPONSE: 1001" or "RESPONSE: NOT_READY"
		}
	}

	private int parseRpcId(String serverLine) throws IOException {
		// e.g. "RESPONSE: 1001"
		if (!serverLine.startsWith("RESPONSE:")) {
			throw new IOException("Invalid server response: " + serverLine);
		}
		String val = serverLine.substring("RESPONSE:".length()).trim();
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid rpcId in response: " + val);
		}
	}

	private String parseResponse(String serverLine) throws IOException {
		// e.g. "RESPONSE: NOT_READY" or "RESPONSE: 8" or "RESPONSE: [1, 2, 3]"
		if (!serverLine.startsWith("RESPONSE:")) {
			throw new IOException("Invalid server response: " + serverLine);
		}
		return serverLine.substring("RESPONSE:".length()).trim();
	}

	// Quick test
	public static void main(String[] args) {
		String host = Constants.IP.CLIENT_IP;
		int port = Constants.Ports.RPC_PORT;
//		// Usage: java AsyncRPCClient <server_ip> <port>
//		if (args.length < 2) {
//			System.out.println("Usage: java AsyncRPCClient <server_ip> <port>");
//			return;
//		}
//		String host = args[0];
//		int port = Integer.parseInt(args[1]);



		AsyncRPCClient client = new AsyncRPCClient(host, port);
		try {
			// Example: asynchronous foo
			int fooId = client.handleAsyncFoo(999990000);
			System.out.println("[Client] asyncFoo(1000000) => rpcId=" + fooId);

			// Example: asynchronous add
			int addId = client.handleAsyncAdd(3343, 53323);
			System.out.println("[Client] asyncAdd(3343,53323) => rpcId=" + addId);

			// Example: asynchronous sort
			int[] arr = {5, 9, 1, 3, 2};
			int sortId = client.handleAsyncSort(arr);
			System.out.println("[Client] asyncSort([5,9,1,3,2]) => rpcId=" + sortId);

			// Meanwhile, do other work, then poll for results:
			for (int i = 0; i < 10; i++) {
				Thread.sleep(500);
				String fooResult = client.getResult(fooId);
				System.out.println("[Client] getResult(fooId) => " + fooResult);
				if (!"NOT_READY".equals(fooResult)) {
					break;
				}
			}

			// Check add result
			String addResult = client.getResult(addId);
			System.out.println("[Client] getResult(addId) => " + addResult);

			// Check sort result
			String sortResult = client.getResult(sortId);
			System.out.println("[Client] getResult(sortId) => " + sortResult);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
