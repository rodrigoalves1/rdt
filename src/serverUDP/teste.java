package serverUDP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class teste {

	public static void main(String[] args) {
		
		String http  = "GET /index.html HTTP/1.1\r\n"+
				"Host: localhost:6788\r\n"+
				"Connection: keep-alive\r\n"+
				"Cache-Control: max-age=0\r\n"+
				"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n"+
				"User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36\r\n"+
				"Accept-Encoding: gzip, deflate, sdch\r\n"+
				"Accept-Language: en-US,en;q=0.8\r\n"+
				"/index.html\r\n\r\n";
byte[] fileBytes = http.getBytes();		

if(!http.equals("")){
String fileName = "";
int i = 5;
while (http.charAt(i) !=  ' ') {
	i++;
}
http = http.substring(5,i);
		System.out.println(http);
		
		
		File file = new File("projeto.pdf");
		//lendo os bytes do arquivo 
		FileInputStream fis;
		try {
			System.out.println("sdfasdf");
			fis = new FileInputStream(file);
			fileBytes = new byte[(int) file.length()];
			fis.read(fileBytes);
			fis.close();
		} catch (FileNotFoundException e) {
			System.out.println("not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("not found1");
			e.printStackTrace();
		}
		
}		
}

}
