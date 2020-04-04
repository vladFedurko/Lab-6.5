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
    static final String SERVER_ADDRESS = "192.168.0.101";

    private boolean toStopAll = false;
    private ArrayList<String> ipList;
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
                    checkIfStop();
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
                    tab.write(senderName + " (" + address + "): " + message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Ошибка в работе сервера", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }).start();

        startServerThread();
    }

    private PrivateTab createNewPrivateTab(String senderName, String address) {
        PrivateTab tab = new PrivateTab(this, senderName, address);
        tabbedPane.addTab(senderName, tab);
        return tab;
    }

    public void activateTab (String name) {
        int index = this.getIndexByNameInTabbedPane(name);
        if(index == -1) {
            tabbedPane.addTab(name, new PrivateTab(this, name, getIpByNameInUsersList(name)));
        } else
            tabbedPane.setSelectedIndex(index);
    }

    public void sendMessage(String message, String senderName, String destinationAddress) {
        try {
            if (senderName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите имя отправителя", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (destinationAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите адрес узла-получателя", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите текст сообщения", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
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

    private PrivateTab findByIpInTabbedPane(String ip) {
        PrivateTab tab = null;
        for(int i = 0; i < tabbedPane.getTabCount(); ++i) {
            tab = (PrivateTab)tabbedPane.getComponentAt(i);
            if(tab.getIp().equals(ip)) {
                return tab;
            }
        }
        return tab;
    }

    private int getIndexByNameInTabbedPane(String name) {
        for(int i = 0; i < tabbedPane.getTabCount(); ++i) {
            if(((PrivateTab)tabbedPane.getComponentAt(i)).getInterlocutorName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void startServerThread() {
        new Thread(() -> {
            Socket socket;
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(SERVER_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            startReceiveThread();
            while(!Thread.interrupted()){
                try {
                    checkIfStop();
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("GET");
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    String response = input.readUTF();
                    if(response.equals("OK")) {
                        ipList = new ArrayList<>(10);
                        while(input.available() > 0) {
                            ipList.add(input.readUTF());
                        }
                        this.updateOnlineUsersList();
                    } else
                    {
                        if(response.startsWith("ERROR:")) {
                            response = response.substring(7);
                            if(response.equals("NEED LOGIN")) {
                                toLoginAgain();
                            }
                        }
                    }
                    sendMulticastNotification(datagramSocket);
                    Thread.sleep(12000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void toLoginAgain() {
        JOptionPane.showMessageDialog(MainFrame.this,
                "Требуется вход", FRAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
        this.setVisible(false);
        toStopAll = true;
        StartWindow frame = new StartWindow();
        frame.setMainFrameCreation(false);
        frame.setFrameToCall(this);
    }

    private void sendMulticastNotification(DatagramSocket socket) throws IOException {
        DatagramPacket dgram;
        dgram = new DatagramPacket(name.getBytes(), name.getBytes().length);
        for (String u : ipList) {
            dgram.setAddress(InetAddress.getByName(u));
            socket.send(dgram);
        }
        socket.close();
    }

    private void startReceiveThread() {
        new Thread(() -> {
            byte[] b = new byte[1000];
            DatagramPacket dgram = new DatagramPacket(b, b.length);
            MulticastSocket socket;
            long time;
            while (!Thread.interrupted()) {
                checkIfStop();
                try {
                    socket = new MulticastSocket(SERVER_PORT);
                    for(String u : ipList) {
                        socket.joinGroup(InetAddress.getByName(u));
                    }
                    socket.setSoTimeout(12000);
                    time = System.currentTimeMillis() + 12000;
                    while (time < System.currentTimeMillis()) {
                        socket.receive(dgram);
                        String msg = new String(dgram.getData(), dgram.getOffset(), dgram.getLength());
                        this.addName(msg, dgram.getAddress().getHostAddress());
                    }
                } catch (SocketTimeoutException ignored) {

                }
                catch (IOException e) {
                    e.printStackTrace();
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
        usersList.removeIf(user -> !ipList.contains(user.getIp()));
        Object[] objects = usersList.toArray();
        int size = objects.length;
        String[] strings = new String[size];
        for (int i = 0; i < size; i++)
            strings[i] = objects[i].toString();
        ((PrivateTab)tabbedPane.getSelectedComponent()).setUsersList(strings);
    }

    private synchronized void checkIfStop() {
        if(toStopAll) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    synchronized void continueAll() {
        setVisible(true);
        toStopAll = false;
        notifyAll();
    }
}