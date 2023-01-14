package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * Chat server that handles message communication with multiple clients.
 * @author u.a.: roegerhe
 *
 */
public class ChatServer extends Thread {

	/**
	 * The port that the server listens on.
	 */
	private static final int PORT = 9001;

	private ServerSocket server;

	/**
	 * The set of all registered users with their output printers
	 */
	private static HashMap<String, PrintWriter> registeredUsers = new HashMap<String, PrintWriter>();

	private List<ClientHandler> clients = new LinkedList<>();

	

	public ChatServer() throws IOException {
		// TODO initialize a Server
		server = new ServerSocket(PORT);
	}

	public void run() {
		while (true){
			try {
				Socket client = server.accept();
				try {
					ClientHandler handler = new ClientHandler(client);
					this.clients.add(handler);
					handler.start();
				}catch (IOException e){
					System.out.println("something went wrong in creating client with address: " + client.getRemoteSocketAddress());
				}

			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		// TODO accept new connections and instantiate a new ClientHandler for each session

	}


	public static void main(String[] args) {
		ChatServer server = null;
		try {
			server = new ChatServer();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("cant create server");
			return;
		}
		server.start();



		/* command line interaction for server */
		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

		while (true){
			try {
				String line;
				if ((line = consoleReader.readLine()) != null){
					if (line.equalsIgnoreCase("quit")){
						server.clients.forEach(clientHandler-> {
							registeredUsers.remove(clientHandler.username);
							clientHandler.closeEverything();
						});
						server.server.close();
						System.exit(0);
						return;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// TODO: listen for commands from commandLine and shut down server on
		// "quit" command.

	}
	
	
	/********************* session class ******************************************************/
	
	/**
	 * A handler thread class to start a new session for each client.
	 * Responsible for a dealing with a single client and sending its messages
	 * to the respective user.
	 */
	private class ClientHandler extends Thread {

		private Socket client;
		private PrintWriter clientOut;
		private BufferedReader clientIn;
		private String username;

		/**
		 * Constructs a handler thread.
		 */
		public ClientHandler(Socket socket) throws IOException {
			this.client = socket;
			clientOut = new PrintWriter(socket.getOutputStream(), true);
			clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			System.out.println("client connected with address: " + client.getRemoteSocketAddress());
			// TODO init handler
		}

		/**
		 * closes this client forced from the server
		 */
		public synchronized void closeEverything(){
			try {
				clientOut.println("quit");
				this.client.close();
				registeredUsers.remove(this.username);
				clients.remove(this);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("cant close this client with address: " + client.getRemoteSocketAddress());
				if (username != null){
					System.out.println("username of problem thread: " + this.username);
				}
			}
		}

		/**
		 * Services this thread's client by repeatedly requesting a screen name
		 * until a unique one has been submitted, then acknowledges the name and
		 * registers the output stream for the client in a global set.
		 */
		public void run() {
			clientOut.println("enter a username: ");
			while (username == null && !client.isClosed()){
				String line;
				try {
					if ((line = clientIn.readLine()) != null){
						if (line.equalsIgnoreCase("quit")){
							this.closeEverything();
							return;
						}
						synchronized (this){
							if (!registeredUsers.containsKey(line)){
								System.out.println("client with address: " + client.getRemoteSocketAddress() + " tries to set username: " + line);
								clientOut.println("username set to: " + line);
								clientOut.println("available userers are:");
								clientOut.println(Arrays.toString(registeredUsers.keySet().toArray()));
								registeredUsers.put(line, clientOut);
								this.username = line;
							}else {
								clientOut.println("username already taken, choose another one:");
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					try {
						client.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					return;
				}
			}
			while (true && !client.isClosed()){
				try {
					String line;
					if ((line = clientIn.readLine()) != null){
						if (line.equalsIgnoreCase("quit")){
							this.closeEverything();
							return;
						}
						String[] data = line.split(":");
						if (data.length != 2) {
							System.out.println("Client with username: " + username + " send a message with wrong format: " + line);
							clientOut.println("usr message has a wrong format, the server has no idea how to handle it");
							continue;
						}
						String username = data[0];
						String message = data[1];
						if (!registeredUsers.containsKey(username)){
							clientOut.println("there is no user with username: " + username);
							System.out.println("client with username: " + username + " tried to send ths message to an invalid user: " + line);
							continue;
						}
						synchronized (this){
							PrintWriter writer = registeredUsers.get(username);
							if (writer != null){
								writer.println(this.username + ":" + message);
								clientOut.println("message successful send");
							}
						}

					}
				} catch (IOException e) {
					if (e instanceof SocketException){
						return;
					}
					e.printStackTrace();
					return;
				}

			}



			/*
			 * TODO: Request a name from this client. Keep requesting until a
			 * name is submitted that is not already used. Note that checking
			 * for the existence of a name and adding the name must be done
			 * while locking the set of names.
			 */

			/*
			 * TODO Now that a successful name has been chosen, display all
			 * available users to the client.
			 */

			/*
			 * TODO: Here comes the interesting part: Listen for input from the
			 * client. Falls "quit": User von der Liste der User entfernen,
			 * Thread schließen 
			 * Ansonsten: Nachricht deserialisieren und an den
			 * Zieluser weiterleiten. Falls der User nicht verfügbar ist, eine
			 * Fehlermeldung auf der Server-Konsole ausgeben.
			 */
		}
	}
}