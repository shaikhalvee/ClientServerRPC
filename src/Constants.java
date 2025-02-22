public interface Constants {
	interface Modes {
		String SERVER = "server";
		String CLIENT = "client";
	}
	interface Modules {
		String TCP = "tcp";
		String UDP = "udp";
		String MTCP = "mtcp";
		String SRPC = "srpc";
		String ARPC = "arpc";
	}
	interface IP {
		String SERVER_IP = "192.168.5.123";
		String CLIENT_IP = "127.0.0.1";
		String LOCALHOST = "127.0.0.1";
	}
	interface Ports {
		int LOCAL_PORT = 8080;
		int SERVER_PORT = 8088;
	}

	interface FilePath {
		String SERVER_IMAGE_FOLDER = "./image/server";
		String CLIENT_IMAGE_FOLDER = "./image/client";
	}
}
