import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

public class MainServer {
    private static final int SERVER_PORT = 5555;

    private ArrayList<User> users = new ArrayList<>(10);
    private HashSet<UserDTO> usersDTO = new HashSet<>();
    
    public static void main(String[] args){
        new MainServer(args);
    }

    private void addCleaner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized(usersDTO) {
                        usersDTO.removeIf(u -> System.currentTimeMillis() - u.getLastCheck().getTime() > 30000);
                    }
                }
            }
        }).start();
    }

    public MainServer(String[] args) {
        addCleaner();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                    while(!Thread.interrupted()){
                        Socket socket = serverSocket.accept();
                        String userIp = socket.getInetAddress().getHostAddress();
                        
                        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                        String state = inputStream.readUTF();
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        
                        if (state.equals("GET")) {
                            UserDTO userDTO = getIfOnline(userIp);
                            if (userDTO == null) {
                                out.writeUTF("ERROR: NEED LOGIN");
                            } else
                            {
                                userDTO.setLastCheck(new Date());
                                out.writeUTF("OK");
                                sendUsersInform(out, userIp);
                            }
                        } else {
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
                        }
                        out.close();
                        inputStream.close();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            UserDTO user1 = new UserDTO(userIp, name, new Date());
            addUserDTO(user1);
        }
    }

    private void sendUsersInform (DataOutputStream out, String userIp) throws IOException {
        synchronized (usersDTO) {
            for (UserDTO userDTO : usersDTO) {
                if (!userDTO.getIp().equals(userIp)) {
                    out.writeUTF(userDTO.getIp());
                }
            }
        }
    }
    
    private void toLogIn (User user, String ip, String password, DataOutputStream out) throws IOException {
        if(user == null) {
            out.writeUTF("ERROR: Wrong name");
            return;
        }
        if(user.getPassword().equals(password)) {
            out.writeUTF("OK");
            UserDTO u = new UserDTO(ip, user.getName(), new Date());
            addUserDTO(u);
        } else
        {
            out.writeUTF("ERROR: Wrong password");
        }
    }

    private void addUserDTO (UserDTO user) {
        synchronized (usersDTO) {
            usersDTO.add(user);
        }
    }
    
    private UserDTO getIfOnline(String ip) {
        synchronized (usersDTO) {
            for (UserDTO userDTO : usersDTO) {
                if (userDTO.getIp().equals(ip)) {
                    return userDTO;
                }
            }
        }
        return null;
    }
    
    private User findByName(String name) {
        for (User user : users) {
            if(user.getName().equals(name))
                return user;
        }
        return null;
    }
}