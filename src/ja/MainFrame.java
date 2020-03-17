package ja;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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
    public static final String SERVER_ADDRESS = "192.168.0.107";
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;

    private boolean toStopAll = false;
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
                        .addComponent(scrollPaneIncoming)
                        .addComponent(messagePanel))
                .addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneIncoming)
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
            while(!Thread.interrupted()){
                try {
                    checkIfStop();
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("GET");
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    String response = input.readUTF();
                    if(response.equals("OK")) {
                        usersList = new ArrayList<>(10);
                        while(input.available() > 0) {
                            usersList.add(new User(input.readUTF()));
                        }
                    } else
                    {
                        if(response.startsWith("ERROR:")) {
                            response = response.substring(7);
                            if(response.equals("NEED LOGIN")) {
                                toLoginAgain();
                            }
                        }
                    }
                    sendMulticastNotification();
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

    private void sendMulticastNotification() throws IOException {
        DatagramSocket socket = new DatagramSocket(SERVER_PORT);
        DatagramPacket dgram;
        dgram = new DatagramPacket(name.getBytes(), name.getBytes().length);
        for (User u : usersList) {
            dgram.setAddress(InetAddress.getByName(u.getIp()));
            socket.send(dgram);
        }
        socket.close();
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