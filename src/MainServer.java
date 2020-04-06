import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainServer {
    private static final int SERVER_PORT = 5555;

    private ArrayList<User> users = new ArrayList<>(10);
    
    public static void main(String[] args){
        new MainServer(args);
    }

    public MainServer(String[] args) {
        new Thread(() -> {
            try {
                final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                while(!Thread.interrupted()){
                    Socket socket = serverSocket.accept();
                    String userIp = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    String state = inputStream.readUTF();
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                    String name = inputStream.readUTF();
                    String password = inputStream.readUTF();
                    User user = findByName(name);
                    if (state.equals("LOGIN")) {
                        toLogIn(user, userIp, password, out);
                    } else
                    {
                        if (state.equals("REGISTRATION")) {
                            toRegister(user, userIp, name, password, out);
                        }
                    }
                    out.close();
                    inputStream.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void toRegister(User user, String userIp, String name, String password, DataOutputStream out) throws IOException {
        if (user != null) {
            out.writeUTF("ERROR: This user already exist");
        } else
        {
            User u = new User(name, password);
            users.add(u);
            out.writeUTF("OK");
        }
    }
    
    private void toLogIn (User user, String ip, String password, DataOutputStream out) throws IOException {
        if(user == null) {
            out.writeUTF("ERROR: Wrong name");
            return;
        }
        if(user.getPassword().equals(password)) {
            out.writeUTF("OK");
        } else
        {
            out.writeUTF("ERROR: Wrong password");
        }
    }
    
    private User findByName(String name) {
        for (User user : users) {
            if(user.getName().equals(name))
                return user;
        }
        return null;
    }
}