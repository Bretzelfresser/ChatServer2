package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * listener-thread reads from messages coming
 * 
 * @author roegerhe
 *
 */
public class ChatClientReceiver extends Thread {
	private boolean runValue;
	private Socket client;
	private BufferedReader reader;
	private ChatClient chatClient;


	ChatClientReceiver(Socket socket, ChatClient chatClient) throws IOException {
		/*
		 * TODO:
		 * Initialize the reader to listen to messages from the server.
		 */
		setRunValue(true);
		this.chatClient = chatClient;
		this.client = socket;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}


	public void run() {
		while (runValue) {
			try {
				String line;
				if ((line = reader.readLine()) != null){
					if (line.equalsIgnoreCase("quit")){
						this.client.close();
						chatClient.commandLineReader.close();
						System.out.println("das hier sollte geschlossen werden");
						System.exit(0);
						return;
					}
					System.out.println(line);
				}
			} catch (IOException e) {
				System.out.println("client is closed: " + client.isClosed());
				setRunValue(false);
				return;
			}
			/*
			 * TODO: Read messages from server
			 * Print them on the comamnd line.
			 */
		}
	}

	public void setRunValue(boolean runValue) {
		this.runValue = runValue;
	}
}
