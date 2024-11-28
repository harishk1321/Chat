  import java.io.*;
import java.net.*;
import java.util.*;

// Main class that contains both server and client functionalities
public class ChatApp {
    public static final int PORT = 12345; // Port number for the server

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose mode:");
        System.out.println("1. Start Chat Server");
        System.out.println("2. Start Chat Client");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume the newline

        if (choice == 1) {
            new ChatServer().startServer();
        } else if (choice == 2) {
            System.out.print("Enter server address (localhost for local): ");
            String serverAddress = scanner.nextLine();
            new ChatClient(serverAddress, PORT).startClient();
        } else {
            System.out.println("Invalid choice. Please restart the program.");
        }
        
        scanner.close();
    }
}

// ChatServer class
class ChatServer {
    public static Set<ClientHandler> clientHandlers = new HashSet<>();

    public void startServer() {
        System.out.println("Chat server is running...");
        try (ServerSocket serverSocket = new ServerSocket(ChatApp.PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Error in server: " + e.getMessage());
        }
    }

    public static void broadcastMessage(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("A client has disconnected");
    }
}

// Handles individual clients on the server
class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received: " + clientMessage);
                ChatServer.broadcastMessage(clientMessage, this);
            }
        } catch (IOException e) {
            System.out.println("Error in client handler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            ChatServer.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}

// ChatClient class
class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to the chat server");
        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        }
    }

    public void startClient() {
        if (socket == null) return;

        // Start a thread to listen for server messages
        new Thread(new ServerListener()).start();

        // Read user input and send messages
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter message: ");
            String message = scanner.nextLine();
            out.println(message);
        }
    }

    private class ServerListener implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }
}


