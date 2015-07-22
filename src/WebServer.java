
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class WebServer {

	// Buffer de pacotes recebidos
	ArrayList<Pacote> received = new ArrayList<Pacote>();
	static byte[] receivedData = new byte[Emissor.MSS + 83];
	// N�mero de sequ�ncia do pacote o qual est� esperando (inicialmente 0)
	int waitingFor = 0;
	boolean end = false;
	// Probabilidade de perda do ACK
	public static final double PROBABILITY = 35;
	ModuloDescarte md = new ModuloDescarte();
	public static void main(String argv[]) throws Exception {
		// Set the port number.
		int port = 9876;
		// Establish the listen socket.
		ServerSocket serverSocket = new ServerSocket(port);
		DatagramSocket fromSender = new DatagramSocket(port+1);

		// Process HTTP service requests in an infinite loop.
		while (true) {
			System.out.println("Esperando pacotes");
			// Listen for a TCP connection request.
			Socket connectionSocket = serverSocket.accept();

			// Cria datagrama e recebe pacote
			DatagramPacket receivedPacket = new DatagramPacket(receivedData,
					receivedData.length);
			fromSender.receive(receivedPacket);

			//if (connectionSocket != null) {
				System.out.println("entrou tcp");
				// Construct an object to process the HTTP request message.
				HttpRequest request = new HttpRequest(connectionSocket);

				// Create a new thread to process the request.
				Thread thread = new Thread(request);

				// Start the thread.
				thread.start();
			//} else if (receivedPacket != null) {
				System.out.println("entrou udp");
//
			//}

		}

	}

	private void handleUDP(DatagramSocket fromSender,
			DatagramPacket receivedPacket) throws ClassNotFoundException, IOException {
		while (!end) {

			// Espera por pacote
			System.out.println("Esperando pacotes");

			// Transformar os bytes recebidos do pacote em um objeto manipul�vel
			Pacote packet = (Pacote) Serializer.toObject(receivedPacket
					.getData());

			System.out.println("Pacote com n�mero de sequ�ncia "
					+ packet.getSeq() + " recebido (�ltimo? "
					+ (packet.isLast() ? "Sim" : "N�o") + " )");

			// Se o pacote recebido for o esperado e for o �ltimo da
			// transmiss�o,
			if (packet.getSeq() == waitingFor && packet.isLast()) {

				// Esperar pelo pr�ximo pacote da sequ�ncia
				waitingFor++;

				// Adicionar o pacote ao buffer
				received.add(packet);

				System.out.println("�ltimo pacote recebido");

				// Fim de transmiss�o
				end = true;

				// Se pacote recebido for o esperado,
			} else if (packet.getSeq() == waitingFor) {

				// Esperar pelo pr�ximo pacote da sequ�ncia
				waitingFor++;

				// Adicionar o pacote ao buffer
				received.add(packet);
				System.out.println("Pacote armazenado no buffer");

				// Caso pacote recebido n�o seja o esperado, n�o o coloca no
				// buffer (descarta-se o pacote)
			} else {
				System.out.println("Pacote ignorado, n�o estava em ordem");
			}

			// Cria pacote ACK
			Ack ackObject = new Ack(waitingFor);

			// Transformar o objeto manipul�vel em bytes
			byte[] ackBytes = Serializer.toBytes(ackObject);

			// Cria um datagrama para o pacote ACK
			DatagramPacket ackPacket = new DatagramPacket(ackBytes,
					ackBytes.length, receivedPacket.getAddress(),
					receivedPacket.getPort());

			// Envia o pacote ACK de acordo com a probabilidade de perda
			if (md.gerarRandom() > PROBABILITY) {
				fromSender.send(ackPacket);
			} else {
				// Caso o n�mero gerado seja menor que o valor definido, a
				// mensagem � descartada
				System.out.println("[X] Ack perdido, n�mero de sequ�ncia "
						+ ackObject.getPacket());
			}

			System.out
					.println("Enviando ACK para o n�mero de sequ�ncia "
							+ waitingFor + " com tamanho " + ackBytes.length
							+ " bytes");

		}
	}
}
