import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiThreadedTCPClient {

	private static final String[] fileList = {"my_image.jpg", "my_image2.jpg", "my_image3.jpg", "my_image4.jpg",
			"my_image5.jpg"};
	final static String IMAGE_FOLDER = Constants.FilePath.CLIENT_IMAGE_FOLDER;
	final static String SERVER_IP = Constants.IP.LOCALHOST;
	final static int PORT = Constants.Ports.SERVER_PORT;
	final static int BUFFER_SIZE = Constants.Config.BUFFER_SIZE;

	private static final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

	public static void main(String[] args) {
		int totalLoop = 1;
		if (args.length != 0) {
			totalLoop = Integer.parseInt(args[0]);
		}
		// run for totalLoop times
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < totalLoop; i++) {
			for (String fileName : fileList) {
				// Each request in a separate thread
				Thread thread = new Thread(() -> fetchFile(fileName));
				thread.start();
				threads.add(thread);
			}
			try {
				for (Thread thread : threads) {
					thread.join();
//					System.out.println("[Client] " + thread.getName() + " finished");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!responseTimes.isEmpty()) {
			long sum = 0;
			for (long responseTime : responseTimes) {
				sum += responseTime;
			}
			double avgNano = sum / (double) responseTimes.size();
			double avgMilli = avgNano / 1000000.0;  // convert ns -> ms

			System.out.println("[Client] Average response time: " + avgMilli + " ms for " + totalLoop + " loops" );
		}
	}

	private static void fetchFile(String fileName) {
		long startTime = System.nanoTime();
		try (Socket socket = new Socket(SERVER_IP, PORT)) {
			PrintWriter outWriter = new PrintWriter(socket.getOutputStream(), true);
			outWriter.println(fileName);  // request

			BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = inputFromServer.readLine();
			if (line.startsWith("ERROR")) {
				System.err.println("[Client] " + line);
				return;
			}

			// Parse renamed filename
			String renamedFile = line.split(":", 2)[1];
			// Parse: "FILESIZE:<size>"
			line = inputFromServer.readLine();
			long fileSize = Long.parseLong(line.split(":", 2)[1]);

			File outputFile = new File(IMAGE_FOLDER, renamedFile);
			FileOutputStream fos = new FileOutputStream(outputFile);
			InputStream socketInputFromServer = socket.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			long totalRead = 0;
			int bytesRead;
			while (totalRead < fileSize && (bytesRead = socketInputFromServer.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
				totalRead += bytesRead;
			}
			fos.flush();
			System.out.println("[Client] Downloaded " + fileName + " as " + renamedFile +
					" in thread: " + Thread.currentThread().getName());
			outWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		long elapsedTimeInNano = endTime - startTime;
		responseTimes.add(elapsedTimeInNano);
		System.out.println("[Client] Elapsed time: " + elapsedTimeInNano + " ns");
	}
}
