

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";

	Socket socket;

	// Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;

	}

	// Implement the run() method of the Runnable interface.
	public void run() {
		try {

			processRequest();

		} catch (Exception e) {
			System.out.println(e + "!!");

		}

	}

	private void processRequest() throws Exception {
		// Get a reference to the socket's input and output streams.
		InputStream is = this.socket.getInputStream();
		DataOutputStream os = new DataOutputStream(
				this.socket.getOutputStream());

		// Set up input stream filters.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Get the request line of the HTTP request message.
		String requestLine = br.readLine();

		// Display the request line.
		System.out.println();
		System.out.println("RL: " + requestLine);

		// Get and display the header lines.
		String headerLine = null;
		String boundary = "";
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
			if (headerLine.indexOf("Content-Type: multipart/form-data") != -1) {
				boundary = headerLine.split("boundary=")[1];
			}
			// The POST boundary

		}

		if (requestLine.startsWith("POST")) {
			System.out.println("entrou");
			String currentLine = "";
			String firstname = "";

			currentLine = br.readLine();
			currentLine = br.readLine();
			currentLine = br.readLine();
			firstname = br.readLine();

			System.out.println("FN = " + firstname);

			String entityBody = "<HTML>"
					+ "<HEAD><TITLE>String manipulation</TITLE></HEAD>"
					+ "<BODY><h1>Sua String em CAIXA ALTA É:</h1> "
					+ firstname.toUpperCase() + "<br><br><a href='index.html' >Voltar</a></BODY></HTML>";

			String statusLine = "HTTP/1.0 200 OK" + CRLF;
			String contentTypeLine = "Content-type: text/html" + CRLF;

			// Send the status line.
			os.writeBytes(statusLine);

			// Send the content type line.
			os.writeBytes(contentTypeLine);

			// Send a blank line to indicate the end of the header lines.
			os.writeBytes(CRLF);

			os.writeBytes(entityBody);

			// Close streams and socket.
			os.close();
			br.close();
			socket.close();

		} else {

			// Extract the filename from the request line.
			StringTokenizer tokens = new StringTokenizer(requestLine);
			String a = tokens.nextToken(); // skip over the method, which should
											// be "GET"

			String fileName = tokens.nextToken();
			System.out.println(fileName);
			// Prepend a "." so that file request is within the current
			// directory.
			fileName = "." + fileName;

			// Open the requested file.
			FileInputStream fis = null;
			boolean fileExists = true;
			try {
				fis = new FileInputStream(fileName);

			} catch (FileNotFoundException e) {
				fileExists = false;
				fileName = "error.html";
				fis = new FileInputStream(fileName);
			}

			// Construct the response message.
			String statusLine = null;
			String contentTypeLine = null;
			String entityBody = null;
			if (fileExists) {
				statusLine = "HTTP/1.0 200 OK" + CRLF;
				contentTypeLine = "Content-type: " + contentType(fileName)
						+ CRLF;

			} else {
				statusLine = "HTTP/1.0 404 Not Found" + CRLF;
				contentTypeLine = "Content-type: " + contentType(fileName)
						+ CRLF;
			}

			// Send the status line.
			os.writeBytes(statusLine);

			// Send the content type line.
			os.writeBytes(contentTypeLine);

			// Send a blank line to indicate the end of the header lines.
			os.writeBytes(CRLF);

			// Send the entity body.
			sendBytes(fis, os);
			fis.close();

			// Close streams and socket.
			os.close();
			br.close();
			socket.close();
		}

	}

	private static void sendBytes(FileInputStream fis, OutputStream os)
			throws Exception {
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[6144];
		int bytes = 0;

		// Copy requested file into the socket's output stream.

		while ((bytes = fis.read(buffer)) > 0) {

			os.write(buffer, 0, bytes);

		}

	}

	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";

		}

		if (fileName.endsWith(".pdf")) {
			return "application/pdf";

		}

		// image/gif
		if (fileName.endsWith(".gif")) {
			return "image/gif";

		}

		// image/jpeg
		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";

		}
		if (fileName.endsWith(".png")) {
			return "image/png";

		}

		if (fileName.endsWith(".mpeg") || fileName.endsWith(".mp3")) {
			return "audio/mpeg";

		}
		if (fileName.endsWith(".mp4")) {
			return "video/mp4";

		}
		return "application/octet-stream";

	}

}
