import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainServer {
    private static final int SERVER_PORT = 4567;

    public static void main(String[] args) {
        ArrayList<String> ips = new ArrayList<>(5);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket =
                            new ServerSocket(SERVER_PORT);
                    while(!Thread.interrupted()){
                        Socket socket = serverSocket.accept();
                        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        String userIp = inputStream.readUTF();
                        synchronized (ips) {
                            boolean isExist = false;
                            for(String ip : ips){
                                if(!ip.equals(userIp)) {
                                    outputStream.writeUTF(ip);
                                } else {
                                    isExist = true;
                                }
                            }
                            ips.add(userIp);
                        }
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
