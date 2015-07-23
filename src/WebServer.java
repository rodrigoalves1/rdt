
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class WebServer {

	public static void main(String argv[]) throws Exception {
		// Set the port number.
		int port = 9876;
		// Establish the listen socket.
		DatagramSocket serverSocket = new DatagramSocket(port);

		// Process HTTP service requests in an infinite loop.
		while (true) {
			System.out.println("Esperando pacotes");
			// Listen for a TCP connection request.
			//Socket connectionSocket = serverSocket.accept();
			// Cria datagrama e recebe pacote

				HttpRequest request = new HttpRequest(serverSocket);

				// Create a new thread to process the request.
				Thread thread = new Thread(request);

				// Start the thread.
				thread.start();

	}

		}
}
