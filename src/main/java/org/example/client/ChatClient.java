package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ChatClient {

    private int portNumber = 9001;
    public BufferedReader commandLineReader;
    private PrintWriter clientOut;
    private Socket clientSocket;
    private ChatClientReceiver listener;

    ChatClient() throws IOException {
        this.clientSocket = new Socket("127.0.0.1", portNumber);
        clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        listener = new ChatClientReceiver(clientSocket, this);
        listener.start();
        /*
         * TODO
         * make a connection to the chat-server
         * start a ChatClientReceiver to listen to responses from the server and display them on the command line
         * listen for command line inputs
         */


        commandLineReader = new BufferedReader(new InputStreamReader(System.in));

    }

    /**
     * Start a new Chat client and listen to console inputs.
     * "quit" is the command to stop the client
     *
     * @param args
     */
    public static void main(String[] args) {
        ChatClient client = null;
        try {
            client = new ChatClient();
        } catch (IOException e) {
            System.out.println("cant instantiate client");
            return;
        }
        /*
         * TODO: Handle commandLine Input here and shut down ChatClient on "Quit"
         * In order to quit the ChatClientReceiver-Thread, make use of the "runReceiver"-boolean.
         */
        while (true) {
            try {
                String line;
                if ((line = client.commandLineReader.readLine()) != null) {
					if (line.equalsIgnoreCase("quit")){
                        client.clientOut.println("quit");
                        client.clientSocket.close();
                        client.commandLineReader.close();
                        client.listener.setRunValue(false);
                        break;
                    }else {
                        client.clientOut.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
