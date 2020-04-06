package ja;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;

@SuppressWarnings("serial")

public class MainFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int WEB_PORT = 4567;
    static final int SERVER_PORT = 5555;
    private static final int CASTING_PORT = 5556;
    static final String SERVER_ADDRESS = "192.168.0.100";

    private ArrayList<User> usersList = new ArrayList<>();
    private JTabbedPane tabbedPane;

    private String name;

    private MainFrame(String name) {
        super(FRAME_TITLE);
        this.name = name;
        setWindowState();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Start Page", new PrivateTab(this, name, "127.0.0.1"));
        tabbedPane.addChangeListener(changeEvent -> updateOnlineUsersList());

        this.getContentPane().add(tabbedPane);

        new Thread(() -> {
            try {
                final ServerSocket serverSocket = new ServerSocket(WEB_PORT);
                while (!Thread.interrupted()) {
                    final Socket socket = serverSocket.accept();
                    final DataInputStream in = new DataInputStream(socket.getInputStream());
                    final String senderName = in.readUTF();
                    final String message = in.readUTF();
                    socket.close();
                    final String address =
                            ((InetSocketAddress) socket
                                    .getRemoteSocketAddress())
                                    .getAddress()
                                    .getHostAddress();
                    PrivateTab tab = findByIpInTabbedPane(socket.getInetAddress().getHostAddress());
                    if(tab == null) {
                        tab = this.createNewPrivateTab(senderName, address);
                    }
                    tab.write(senderName + ": " + message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Ошибка в работе сервера", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }).start();

        startReceiveThread();
    }

    private PrivateTab createNewPrivateTab(String senderName, String address) {
        PrivateTab tab = new PrivateTab(this, senderName, address);
        tabbedPane.addTab(senderName, tab);
        return tab;
    }

    void activateTab(String name) {
        int index = this.getIndexByNameInTabbedPane(name);
        if(index == -1) {
            tabbedPane.addTab(name, new PrivateTab(this, name, getIpByNameInUsersList(name)));
        } else
            tabbedPane.setSelectedIndex(index);
    }

    void sendMessage(String message, String senderName, String destinationAddress) {
        try {
            if (senderName.isEmpty()) {
                throw new NullPointerException("ja.MainFrame sendMessage senderName is empty");
            }
            if (destinationAddress.isEmpty()) {
                throw new NullPointerException("ja.MainFrame sendMessage destinationAddress is empty");
            }
            if (message.isEmpty()) {
                throw new NullPointerException("ja.MainFrame sendMessage message is empty");
            }
            final Socket socket = new Socket(destinationAddress, WEB_PORT);
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(senderName);
            out.writeUTF(message);
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение: узел-адресат не найден",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTabsWithOfflineUsers() {
        for(int i = 0; i < tabbedPane.getTabCount(); ++i) {
            boolean isExist = false;
            String userIp = ((PrivateTab)tabbedPane.getComponentAt(i)).getIp();
            if(userIp.equals("127.0.0.1")) {
                continue;
            }
            for(User ip : usersList) {
                if(userIp.equals(ip.getIp())) {
                    isExist = true;
                    break;
                }
            }
            if(!isExist) {
                if (tabbedPane.getSelectedIndex() == i) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Пользователь вышел из сети", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                tabbedPane.remove(i);
            }
        }
    }

    private PrivateTab findByIpInTabbedPane(String ip) {
        PrivateTab tab;
        for(int i = 0; i < tabbedPane.getTabCount(); ++i) {
            tab = (PrivateTab)tabbedPane.getComponentAt(i);
            if(tab.getIp().equals(ip)) {
                return tab;
            }
        }
        return null;
    }

    private int getIndexByNameInTabbedPane(String name) {
        for(int i = 0; i < tabbedPane.getTabCount(); ++i) {
            if(((PrivateTab)tabbedPane.getComponentAt(i)).getInterlocutorName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void sendMulticastNotification() throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket dgram;
            dgram = new DatagramPacket(name.getBytes(), name.getBytes().length, InetAddress.getByName("230.1.1.1"), CASTING_PORT);
            socket.send(dgram);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        finally {
            assert socket != null;
            socket.close();
        }
    }

    private void startReceiveThread() {
        new Thread(() -> {
            byte[] b = new byte[1000];
            DatagramPacket dgram = new DatagramPacket(b, b.length);
            MulticastSocket socket = null;
            long time;
            while (!Thread.interrupted()) {
                try {
                    sendMulticastNotification();
                    updateOnlineUsersList();
                    socket = new MulticastSocket(CASTING_PORT);
                    socket.joinGroup(InetAddress.getByName("230.1.1.1"));
                    socket.setSoTimeout(12000);
                    time = System.currentTimeMillis() + 12000;
                    while (time > System.currentTimeMillis()) {
                        socket.receive(dgram);
                        String msg = new String(dgram.getData(), dgram.getOffset(), dgram.getLength());
                        this.addName(msg, dgram.getAddress().getHostAddress());
                    }
                    socket.leaveGroup(InetAddress.getByName("230.1.1.1"));
                    socket.close();
                } catch (SocketTimeoutException ignored) {

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if(socket != null)
                        socket.close();
                }
            }
        }).start();
    }

    private String getIpByNameInUsersList(String name) {
        for(User u : usersList) {
            if(u.getName().equals(name)) {
                return u.getIp();
            }
        }
        return null;
    }

    private void addName(String name, String ip) {
        boolean isExist = false;
        for(User u : usersList) {
            if(u.getIp().equals(ip)) {
                u.setName(name);
                isExist = true;
            }
        }
        if(!isExist) {
            usersList.add(new User(ip, name));
        }
        this.updateOnlineUsersList();
    }

    private void updateOnlineUsersList() {
        usersList.removeIf(user -> user.getLastNotification() < System.currentTimeMillis() - 60000);
        int size = usersList.size();
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = usersList.get(i).getName();
        }
        ((PrivateTab)tabbedPane.getSelectedComponent()).setUsersList(strings);
        deleteTabsWithOfflineUsers();
    }

    private void setWindowState() {
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartWindow::new);
    }

    static void start(JFrame frame, String name) {
        frame.dispose();
        SwingUtilities.invokeLater(() -> {
            final MainFrame frame1 = new MainFrame(name);
            frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame1.setVisible(true);
        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}