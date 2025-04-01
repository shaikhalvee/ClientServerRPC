import java.io.*;
import java.net.*;
import java.util.*;

public class BatchRpcClient {
	public static final String SERVER_IP = "127.0.0.1";
	public static final int SERVER_PORT = 9000;
	public static final int NUM_CALLS = 10;
	public static final long FOO_MIN_ITERS = 100000;
	public static final long FOO_MAX_ITERS = 1000000;
	public static final int ADD_MAX_VAL = 50000;
	public static final int SORT_MIN_LEN = 200;
	public static final int SORT_MAX_LEN = 30000;
	public static final int SORT_VAL_RANGE = 100000;

	public static void main(String[] args) {
		Socket socket = null;
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			// Build the batch request
			String batch = generateBatch();
			out.write(batch);
			out.flush();

			// Read the single combined response from the server
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line).append("\n");
			}
			System.out.println("[BatchClient] Server Response:\n" + response.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(socket != null) {
				try { socket.close(); } catch (IOException e) { }
			}
		}
	}

	private static String generateBatch() {
		StringBuilder batch = new StringBuilder();
		batch.append("REQUEST: BATCH\n");
		Random rand = new Random();
		for (int i = 0; i < NUM_CALLS; i++) {
			int callType = rand.nextInt(3); // 0 = foo, 1 = add, 2 = sort
			if (callType == 0) {
				// Generate a foo call
				long iters = FOO_MIN_ITERS +
						(long) rand.nextInt((int)(FOO_MAX_ITERS - FOO_MIN_ITERS + 1));
				batch.append("foo ").append(iters).append("\n");
			} else if (callType == 1) {
				// Generate an add call
				int a = rand.nextInt(ADD_MAX_VAL);
				int b = rand.nextInt(ADD_MAX_VAL);
				batch.append("add ").append(a).append(" ").append(b).append("\n");
			} else {
				// Generate a sort call
				int length = SORT_MIN_LEN +
						rand.nextInt(SORT_MAX_LEN - SORT_MIN_LEN + 1);
				batch.append("sort ").append(length);
				for (int j = 0; j < length; j++) {
					int val = rand.nextInt(SORT_VAL_RANGE);
					batch.append(" ").append(val);
				}
				batch.append("\n");
			}
		}
		batch.append("END\n");
		return batch.toString();
	}
}
