package ja;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class StartWindow extends JFrame {

    private boolean isLoginState = true;

    private JTextField nameField = new JTextField(15);
    private JTextField passwordField = new JTextField(15);
    private JButton sendButton = new JButton("Принять");
    private JTextField passwordField2 = new JTextField(15);
    private JButton changeStateButton = new JButton();
    private JLabel nameLabel = new JLabel("Логин");
    private JLabel passwordLabel = new JLabel("Пароль");

    private GroupLayout layout;

    private final int width = 300;
    private final int height = 270;

    public StartWindow() {
        super("Вход");
        setSize(new Dimension(width, height));
        setResizable(false);

        layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - width) / 2, (kit.getScreenSize().height - height) / 2);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                send();
            }
        });
        changeStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isLoginState = !isLoginState;
                if(isLoginState)
                    setLoginInterface();
                else
                    setRegisterInterface();
            }
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
                .addGap(MainFrame.LARGE_GAP)
                .addComponent(nameLabel)
                .addGap(MainFrame.SMALL_GAP)
                .addComponent(nameField, 22, 22, 22)
                .addGap(MainFrame.MEDIUM_GAP)
                .addComponent(passwordLabel)
                .addGap(MainFrame.SMALL_GAP)
                .addComponent(passwordField, 22, 22, 22)
                .addGap(MainFrame.MEDIUM_GAP)
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
                        .addGap(MainFrame.LARGE_GAP)
                        .addComponent(nameLabel)
                        .addGap(MainFrame.SMALL_GAP)
                        .addComponent(nameField, 22, 22, 22)
                        .addGap(MainFrame.MEDIUM_GAP)
                        .addComponent(passwordLabel)
                        .addGap(MainFrame.SMALL_GAP)
                        .addComponent(passwordField, 22, 22, 22)
                        .addGap(MainFrame.MEDIUM_GAP)
                        .addComponent(passwordLabel2)
                        .addGap(MainFrame.SMALL_GAP)
                        .addComponent(passwordField2, 22, 22, 22)
                        .addGap(MainFrame.MEDIUM_GAP)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(sendButton)
                                .addComponent(changeStateButton))
                        .addContainerGap()
        );
    }

    private void send() {

    }
}