package ja;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


class StartWindow extends JFrame {

    private boolean isLoginState = true;

    private JTextField nameField = new JTextField(15);

    private JTextField passwordField = new JTextField(15);
    private JButton sendButton = new JButton("Принять");
    private JTextField passwordField2 = new JTextField(15);
    private JButton changeStateButton = new JButton();
    private JLabel nameLabel = new JLabel("Логин");
    private JLabel passwordLabel = new JLabel("Пароль");
    private JLabel errorLabel = new JLabel("");
    private GroupLayout layout;

    StartWindow() {
        super("Вход");

        setVisible(true);
        int width = 300;
        int height = 300;
        setSize(new Dimension(width, height));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        layout = new GroupLayout(getContentPane());
        setLayout(layout);

        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - width) / 2, (kit.getScreenSize().height - height) / 2);
        sendButton.addActionListener(actionEvent -> {
            if(isLoginState || passwordField.getText().equals(passwordField2.getText())) {
                try {
                    send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
            {
                errorLabel.setText("Пароли не совпадают");
            }
        });
        changeStateButton.addActionListener(actionEvent -> {
            isLoginState = !isLoginState;
            if(isLoginState)
                setLoginInterface();
            else
                setRegisterInterface();
        });
        setLoginInterface();
    }

    private void setLoginInterface() {
        getContentPane().removeAll();
        JLabel header = new JLabel("Вход");
        changeStateButton.setText("Зарегистрироваться");
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(header, GroupLayout.Alignment.CENTER)
                                .addComponent(changeStateButton, GroupLayout.Alignment.LEADING)
                                .addComponent(nameLabel)
                                .addComponent(errorLabel)
                                .addComponent(nameField)
                                .addComponent(passwordLabel)
                                .addComponent(passwordField)
                                .addComponent(sendButton, GroupLayout.Alignment.TRAILING)))
                .addContainerGap()
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(header)
                .addGap(PrivateTab.SMALL_GAP)
                .addComponent(errorLabel)
                .addGap(PrivateTab.LARGE_GAP)
                .addComponent(nameLabel)
                .addGap(PrivateTab.SMALL_GAP)
                .addComponent(nameField, 22, 22, 22)
                .addGap(PrivateTab.MEDIUM_GAP)
                .addComponent(passwordLabel)
                .addGap(PrivateTab.SMALL_GAP)
                .addComponent(passwordField, 22, 22, 22)
                .addGap(PrivateTab.MEDIUM_GAP)
                .addGroup(layout.createParallelGroup()
                        .addComponent(sendButton)
                        .addComponent(changeStateButton))
                .addContainerGap()
        );
    }

    private void setRegisterInterface() {
        getContentPane().removeAll();
        JLabel header = new JLabel("Регистратура");
        JLabel passwordLabel2 = new JLabel("Повторите пароль");
        changeStateButton.setText("Вход");
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(header, GroupLayout.Alignment.CENTER)
                                        .addComponent(errorLabel)
                                        .addComponent(changeStateButton, GroupLayout.Alignment.LEADING)
                                        .addComponent(nameLabel)
                                        .addComponent(passwordLabel2)
                                        .addComponent(passwordField2)
                                        .addComponent(nameField)
                                        .addComponent(passwordLabel)
                                        .addComponent(passwordField)
                                        .addComponent(sendButton, GroupLayout.Alignment.TRAILING)))
                        .addContainerGap()
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(header)
                        .addGap(PrivateTab.SMALL_GAP)
                        .addComponent(errorLabel)
                        .addGap(PrivateTab.LARGE_GAP)
                        .addComponent(nameLabel)
                        .addGap(PrivateTab.SMALL_GAP)
                        .addComponent(nameField, 22, 22, 22)
                        .addGap(PrivateTab.MEDIUM_GAP)
                        .addComponent(passwordLabel)
                        .addGap(PrivateTab.SMALL_GAP)
                        .addComponent(passwordField, 22, 22, 22)
                        .addGap(PrivateTab.MEDIUM_GAP)
                        .addComponent(passwordLabel2)
                        .addGap(PrivateTab.SMALL_GAP)
                        .addComponent(passwordField2, 22, 22, 22)
                        .addGap(PrivateTab.MEDIUM_GAP)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(sendButton)
                                .addComponent(changeStateButton))
                        .addContainerGap()
        );
    }

    private void send() throws IOException {
        Socket socket = new Socket(MainFrame.SERVER_ADDRESS, MainFrame.SERVER_PORT);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(isLoginState ? "LOGIN" : "REGISTRATION");
        out.writeUTF(nameField.getText());
        out.writeUTF(passwordField.getText());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String state = in.readUTF();
        out.close();
        in.close();
        socket.close();
        if(!state.equals("OK")) {
            if(state.startsWith("ERROR")) {
                errorLabel.setText(state.substring(7));
            }
        } else
        {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            MainFrame.start(this, nameField.getText());
        }
    }
}