package ja;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    public static final int SMALL_GAP = 5;
    public static final int MEDIUM_GAP = 10;
    public static final int LARGE_GAP = 15;
    public static final int WEB_PORT = 4567;
    public static final int SERVER_PORT = 5555;
    public static final String SERVER_ADDRESS = "192.168.0.102";
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;
    private JList<String> onlineUsersList;

    private boolean toStopAll = false;
    private ArrayList<String> ipList;
    private ArrayList<User> usersList;
    private String name;

    public MainFrame(String name) {
        super(FRAME_TITLE);
        this.name = name;
        setWindowState();
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS, 0);
        textAreaIncoming.setEditable(false);
        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS);
        textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS);
        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS, 0);
        textFieldFrom.setText(name);
        textFieldFrom.setEnabled(false);
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);
        final JLabel labelFrom = new JLabel("От");
        final JLabel labelTo = new JLabel("Получатель");
        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));
        final JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        onlineUsersList = new JList<>();
        onlineUsersList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1 && e.getClickCount() == 2) {

                }
            }
        });
        JScrollPane usersListScrollPane = new JScrollPane(onlineUsersList);
        usersListScrollPane.setMaximumSize(new Dimension(120, scrollPaneOutgoing.getMaximumSize().height));

        final GroupLayout layout2 = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout2);
        layout2.setHorizontalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(layout2.createSequentialGroup()
                                .addComponent(labelFrom)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldFrom)
                                .addGap(LARGE_GAP)
                                .addComponent(labelTo)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldTo))
                        .addComponent(scrollPaneOutgoing)
                        .addComponent(sendButton))
                .addContainerGap());
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelFrom)
                        .addComponent(textFieldFrom)
                        .addComponent(labelTo)
                        .addComponent(textFieldTo))
                .addGap(MEDIUM_GAP)
                .addComponent(scrollPaneOutgoing)
                .addGap(MEDIUM_GAP)
                .addComponent(sendButton)
                .addContainerGap());

        final GroupLayout layout1 = new GroupLayout(getContentPane());
        setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addGroup(layout1.createSequentialGroup()
                                .addComponent(scrollPaneIncoming)
                                .addGap(LARGE_GAP)
                                .addComponent(usersListScrollPane))
                        .addComponent(messagePanel))
                .addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addComponent(scrollPaneIncoming)
                        .addComponent(usersListScrollPane))
                .addGap(MEDIUM_GAP)
                .addComponent(messagePanel)
                .addContainerGap());

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
                    textAreaIncoming.append(senderName + " (" + address + "): " + message + "\n");
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

    private void sendMessage() {
        try {
            final String senderName = textFieldFrom.getText();
            final String destinationAddress = textFieldTo.getText();
            final String message = textAreaOutgoing.getText();
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
            textAreaIncoming.append("Я -> " + destinationAddress + ": " + message + "\n");
            textAreaOutgoing.setText("");
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

    private void addName (String name, String ip) {
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
        onlineUsersList.setListData((String[]) usersList.toArray());
        onlineUsersList.repaint();
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

    public static void start(JFrame frame, String name) {
        frame.dispose();
        SwingUtilities.invokeLater(() -> {
            final MainFrame frame1 = new MainFrame(name);
            frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame1.setVisible(true);
        });
    }

    public synchronized void continueAll() {
        setVisible(true);
        toStopAll = false;
        notifyAll();
    }
}