import java.io.Serializable;
import java.util.Arrays;


public class Pacote implements Serializable {

	public int seq;
	
	public byte[] data;
	
	public boolean last;
	public boolean file;
	public Pacote(int seq, byte[] data, boolean last, boolean file) {
		super();
		this.seq = seq;
		this.data = data;
		this.last = last;
		this.file = file;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	@Override
	public String toString() {
		return "UDPPacket [seq=" + seq + ", data=" + Arrays.toString(data)
				+ ", last=" + last + "]";
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}
	
	
}
