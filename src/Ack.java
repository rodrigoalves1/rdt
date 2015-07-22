import java.io.Serializable;


public class Ack implements Serializable{
	//numero do pacote desse ack
	private int packet;

	public Ack(int packet) {
		super();
		this.packet = packet;
	}

	public int getPacket() {
		return packet;
	}

	public void setPacket(int packet) {
		this.packet = packet;
	}
	
	

}
