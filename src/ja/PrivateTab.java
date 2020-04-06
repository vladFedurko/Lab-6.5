package ja;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class PrivateTab extends JPanel {

    static final int SMALL_GAP = 5;
    static final int MEDIUM_GAP = 10;
    static final int LARGE_GAP = 15;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private final JTextField textFieldFrom;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;
    private JList<String> onlineUsersList;

    private String ip;

    private String interlocutorName;

    PrivateTab(MainFrame frame, String interlocutorName, String ip) {
        super();
        this.interlocutorName = interlocutorName;
        this.ip = ip;
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS, 0);
        textAreaIncoming.setEditable(false);
        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS);
        JTextField textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS);
        textFieldTo.setText(interlocutorName);
        textFieldTo.setEnabled(false);
        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS, 0);
        textFieldFrom.setText(frame.getName());
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
                final String senderName = textFieldFrom.getText();
                final String message = textAreaOutgoing.getText();
                frame.sendMessage(message, senderName, ip);
                textAreaIncoming.append("Я (" + frame.getName() + ") : " + message + "\n");
                textAreaOutgoing.setText("");
            }
        });

        onlineUsersList = new JList<>();
        onlineUsersList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1 && e.getClickCount() == 2) {
                    frame.activateTab(onlineUsersList.getSelectedValue());
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

        final GroupLayout layout1 = new GroupLayout(this);
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
    }

    void setUsersList(String[] array) {
        onlineUsersList.setListData(array);
        onlineUsersList.repaint();
    }

    void write(String message) {
        textAreaIncoming.append(message);
    }

    String getIp() {
        return ip;
    }

    String getInterlocutorName() {
        return interlocutorName;
    }
}