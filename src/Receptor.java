import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;


public class Receptor{
	static DatagramPacket receivedPacket = null;
	// Probabilidade de perda do ACK
	public static final double PROBABILITY = 1;
	// Maximum Segment Size - Quantidade de dados da camada de aplicação no segmento
	public static final int MSS = 4000;

	// Janela de envio - numero de pacotes que podem ser enviados sem o recebmento de um ack
	public static final int JANELA = 30;
	
	// Tempo em milisegundos para estouro do timer e reenvio dos pacotes sem ack
	public static final int TIMER = 300;
	public static void main(String[] args) throws Exception{
		ModuloDescarte md = new ModuloDescarte();
		// Cria socket do receptor
		DatagramSocket fromSender = new DatagramSocket(9876);
		
		// 83 é o tamanho base (em bytes) do Pacote (inclui outros cabeçalhos)
		byte[] receivedData = new byte[Emissor.MSS + 91];

		// Número de sequência do pacote o qual está esperando (inicialmente 0)
		int waitingFor = 0;

		// Buffer de pacotes recebidos
		ArrayList<Pacote> received = new ArrayList<Pacote>();
		
		boolean end = false;

		// Enquanto envio não terminar,
		while(!end){
			
			// Espera por pacote
			System.out.println("Esperando pacotes");

			// Cria datagrama e recebe pacote
			 receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			 
			 
			 
			fromSender.receive(receivedPacket);

			// Transformar os bytes recebidos do pacote em um objeto manipulável
			Pacote packet = (Pacote) Serializer.toObject(receivedPacket.getData());
			
			System.out.println("Pacote com número de sequência " + packet.getSeq() + " recebido (último? " + (packet.isLast() ? "Sim":"Não") + " )");
			System.out.println("RECEBENDO PACOTES DE: " + receivedPacket.getAddress() + " " +receivedPacket.getPort());
			// Se o pacote recebido for o esperado e for o último da transmissão,
			if(packet.getSeq() == waitingFor && packet.isLast()){
				
				// Esperar pelo próximo pacote da sequência
				waitingFor++;
				
				// Adicionar o pacote ao buffer
				received.add(packet);
				
				System.out.println("Último pacote recebido");
				
				// Fim de transmissão
				end = true;
				
			// Se pacote recebido for o esperado,
			}else if(packet.getSeq() == waitingFor){
				
				// Esperar pelo próximo pacote da sequência
				waitingFor++;
				
				// Adicionar o pacote ao buffer
				received.add(packet);
				System.out.println("Pacote armazenado no buffer");
			
			// Caso pacote recebido não seja o esperado, não o coloca no buffer (descarta-se o pacote)
			}else{
				System.out.println("Pacote ignorado, não estava em ordem");
			}

			// Cria pacote ACK
			Ack ackObject = new Ack(waitingFor);

			// Transformar o objeto manipulável em bytes
			byte[] ackBytes = Serializer.toBytes(ackObject);

			// Cria um datagrama para o pacote ACK
			DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			// Envia o pacote ACK de acordo com a probabilidade de perda
			if(md.gerarRandom() > PROBABILITY){
				fromSender.send(ackPacket);
				
			}else{
				// Caso o número gerado seja menor que o valor definido, a mensagem é descartada
				System.out.println("[X] Ack perdido, número de sequência " + ackObject.getPacket());
			}

			System.out.println("Enviando ACK para o número de sequência " + waitingFor + " com tamanho " + ackBytes.length  + " bytes");


		}

		// Cria um arquivo para os pacotes recebidos
		System.out.println(" ------------ REQUISICAO RECEBIDA ---------------- ");
		String httpRequest = "";
		
		for(Pacote p : received){
			for(byte b: p.getData()){
				httpRequest+=(char)b;
			}
		}
		System.out.println(httpRequest);
		enviar(httpRequest,receivedPacket);
		
		
		


	}

	
	
	public static void enviar(String httpa,DatagramPacket destination) throws ClassNotFoundException, IOException{//Modulo para gerar numero randomico 
		ModuloDescarte md = new ModuloDescarte();
		boolean isfile = false;
		
		System.out.println(destination.getAddress() + " -- "+ destination.getPort());
		// Numero de sequência do ultimo pacote enviado mas nao reconhecido 
		int rcvBase = 0;
		
		// Número de sequência do último pacote com ack recebido
		int waitingForAck = 0;

		// Bytes a serem enviados
		String http  = "!";
		byte[] fileBytes = http.getBytes();		
		
		if(!httpa.equals("")){
			String fileName = "";
			int i = 5;
			while (httpa.charAt(i) !=  ' ') {
				i++;
			}
			httpa = httpa.substring(5,i);
			System.out.println("FILE NAME ---- "+httpa);
		//localizando arquivo no computador
		File file = new File(httpa);
		//lendo os bytes do arquivo 
		FileInputStream fis = new FileInputStream(file);
		fileBytes = new byte[(int) file.length()];
		fis.read(fileBytes);
		fis.close();
		isfile = true;
		}
	  
		System.out.println("Tamanho de bytes: " + fileBytes.length + " bytes");

		// Número de pacotes
		int lastSeq = (int) Math.ceil( (double) fileBytes.length / MSS);

		System.out.println("Número de pacotes a serem enviados: " + lastSeq);

		DatagramSocket toReceiver = new DatagramSocket();

		// Endereço de quem irá receber o arquivo
		InetAddress receiverAddress = destination.getAddress();
		
		// Lista para armazenar os pacotes enviados
		ArrayList<Pacote> sent = new ArrayList<Pacote>();

		while(true){

			// Envia arquivos enquanto temos pacotes autorizados dentro da janela e enquanto esse pacote não é o último
			while((rcvBase - waitingForAck) < JANELA && rcvBase < lastSeq){

				// Buffer de envio
				byte[] filePacketBytes = new byte[MSS];

				// Fragmenta parte dos bytes a serem enviados de acordo com o MSS
				filePacketBytes = Arrays.copyOfRange(fileBytes, rcvBase*MSS, rcvBase*MSS + MSS);

				// Cria o pacote 
				Pacote rdtPacketObject = new Pacote(rcvBase, filePacketBytes, (rcvBase == lastSeq-1) ? true : false,isfile);

				// Serializa o Pacote
				byte[] sendData = Serializer.toBytes(rdtPacketObject);

				// Cria o pacote UDP com os dados, endereço receptor e porta receptor
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, destination.getAddress(), 9876);

				System.out.println("Enviando pacote com número de sequência " + rcvBase +  " e tamanho " + sendData.length + " bytes");

				// Coloca os pacotes enviados na lista
				sent.add(rdtPacketObject);
				
				// Envia o pacote de acordo com a probabilidade de perda
				if(md.gerarRandom() > PROBABILITY){
					toReceiver.send(packet);
				}else{
					//caso o numero gerado seja menor que o valor definido a mensagem é descartada
					System.out.println("[X] Pacote perdido com número de sequência " + rcvBase);
				}

				// aumenta o valor do último pacote enviado
				rcvBase++;

			} 
			
			// Array de bytes para os ACKs enviados pelo receptor
			byte[] ackBytes = new byte[40];
			
			// Cria um pacote para o ACK
			DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
			
			try{
				// Inicializa o temporizador com o tempo definido(TIMER). Se um ACK não for recebido antes de terminar o tempo causa uma exceção
				toReceiver.setSoTimeout(TIMER);
				
				// Recebe o ack
				toReceiver.receive(ack);
				
				// Transformar os bytes do pacote em um objeto manipulavel ACK
				Ack ackObject = (Ack) Serializer.toObject(ack.getData());
				
				System.out.println("ACK recebido para o pacote número " + ackObject.getPacket());
				
				// Se o ACK recebido for o do último pacote para o envio
				if(ackObject.getPacket() == lastSeq){
					break;
				}
				//ack esperado agora é o maior numero de sequencia entre o ack atual e o ack esperado
				waitingForAck = Math.max(waitingForAck, ackObject.getPacket());
				
			}catch(SocketTimeoutException e){
				// caso o temporizador estoure, reenvia todos os pacotes não reconhecidos
				//cria um loop do ack esperado até o último autorizado a ser enviado enviado
				for(int i = waitingForAck; i < rcvBase; i++){
					
					// Serializa o pacote em dados
					byte[] sendData = null;
					try {
						sendData = Serializer.toBytes(sent.get(i));
					} catch (IOException e2) {
						e2.printStackTrace();
					}

					// Cria o pacote igual a anteriormente
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, destination.getAddress(), 9876 );
					
					// Envia o pacote de acordo com a probabilidade de perda
					if(md.gerarRandom() > PROBABILITY){
						try {
							toReceiver.send(packet);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}else{
						//caso o numero gerado seja menor que o valor definido a mensagem é descartada
						System.out.println("[X] Pacote perdido com número de sequência " + sent.get(i).getSeq());
					}

					System.out.println("Reenviando pacotes com número de sequência " + sent.get(i).getSeq() +  " e tamanho " + sendData.length + " bytes");
				}
			}
			
		
		}
		
		System.out.println("------------------Resposta finalizada!!-----------------");
		

	}

}