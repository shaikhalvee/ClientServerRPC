import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BatchRpcServer {
	public static final int SERVER_PORT = 9000;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("[BatchServer] Listening on port " + SERVER_PORT + "...");
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("[BatchServer] Client connected.");
				handleClient(clientSocket);
				clientSocket.close();
				System.out.println("[BatchServer] Client disconnected.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try { serverSocket.close(); } catch(IOException e) { }
			}
		}
	}

	private static void handleClient(Socket clientSocket) {
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(clientSocket.getOutputStream()));

			// Read the entire batch request
			StringBuilder requestBuilder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				requestBuilder.append(line).append("\n");
				// Stop reading once the "END" line is received
				if (line.trim().equals("END")) {
					break;
				}
			}
			String request = requestBuilder.toString();

			// Check for the proper header
			if (!request.startsWith("REQUEST: BATCH")) {
				out.write("RESPONSE: ERROR - not a BATCH request\n");
				out.flush();
				return;
			}

			String response = processBatch(request);
			out.write(response);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String processBatch(String batch) {
		StringBuilder response = new StringBuilder();
		response.append("RESPONSE:\n");

		// Split into lines and skip the first line ("REQUEST: BATCH")
		String[] lines = batch.split("\n");
		if (lines.length < 2) {
			response.append("ERROR: Malformed batch request (no newline)\n");
			return response.toString();
		}
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.isEmpty()) continue;
			if (line.equals("END")) break;

			// Tokenize the line
			String[] tokens = line.split("\\s+");
			if (tokens.length == 0) continue;

			String command = tokens[0];
			if ("foo".equals(command)) {
				if (tokens.length < 2) {
					response.append("ERROR(foo): missing iterations\n");
				} else {
					try {
						long iterations = Long.parseLong(tokens[1]);
						long result = foo(iterations);
						response.append(String.format("foo(%d) = %d\n", iterations, result));
					} catch (NumberFormatException e) {
						response.append("ERROR(foo): invalid number format\n");
					}
				}
			} else if ("add".equals(command)) {
				if (tokens.length < 3) {
					response.append("ERROR(add): missing operands\n");
				} else {
					try {
						int a = Integer.parseInt(tokens[1]);
						int b = Integer.parseInt(tokens[2]);
						int sum = add(a, b);
						response.append(String.format("add(%d,%d) = %d\n", a, b, sum));
					} catch (NumberFormatException e) {
						response.append("ERROR(add): invalid number format\n");
					}
				}
			} else if ("sort".equals(command)) {
				if (tokens.length < 2) {
					response.append("ERROR(sort): missing array size\n");
				} else {
					try {
						int n = Integer.parseInt(tokens[1]);
						if (n < 1 || tokens.length - 2 < n) {
							response.append("ERROR(sort): array size mismatch\n");
						} else {
							int[] arr = new int[n];
							boolean valid = true;
							for (int j = 0; j < n; j++) {
								try {
									arr[j] = Integer.parseInt(tokens[j + 2]);
								} catch (NumberFormatException e) {
									response.append("ERROR(sort): invalid number format\n");
									valid = false;
									break;
								}
							}
							if (valid) {
								sortArray(arr);
								response.append("sort => [");
								for (int j = 0; j < n; j++) {
									response.append(arr[j]);
									if (j < n - 1) {
										response.append(",");
									}
								}
								response.append("]\n");
							}
						}
					} catch (NumberFormatException e) {
						response.append("ERROR(sort): invalid array size\n");
					}
				}
			} else {
				response.append("ERROR: Unknown command\n");
			}
		}
		return response.toString();
	}

	// CPU‐intensive function: computes the sum 0 + 1 + ... + (iterations-1)
	private static long foo(long iterations) {
		long sum = 0;
		for (long i = 0; i < iterations; i++) {
			sum += i;
		}
		return sum;
	}

	// Addition
	private static int add(int a, int b) {
		return a + b;
	}

	// Simple bubble sort
	private static void sortArray(int[] arr) {
		int n = arr.length;
		for (int i = 0; i < n - 1; i++) {
			for (int j = 0; j < n - i - 1; j++) {
				if (arr[j] > arr[j + 1]) {
					int tmp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = tmp;
				}
			}
		}
	}
}
