package io.github.defectly;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.function.Consumer;


public class MainWindow extends JFrame {

    private JTextArea chatArea;
    private JTextArea messageArea;
    private Server server;


    MainWindow() {

        try {
            server = new Server("127.0.0.1", 34);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            throw new RuntimeException(exception);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 3, screenSize.height / 3);
//        setSize(screenSize.width / 3, screenSize.height / 3);

        setTitle("ChatServer");
//        setMinimumSize(new java.awt.Dimension(screenSize.width / 3, screenSize.height / 3));

        // Create chat area
        chatArea = new JTextArea(20, 30);
        chatArea.setFont(new java.awt.Font("Dialog", 0, 14));
        chatArea.setLineWrap(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Add components to panels
        JPanel chatPanel = new JPanel();
        chatPanel.add(chatScrollPane);

        JPanel buttons = new JPanel(new GridLayout(1, 2));

        var startButton = new Button("start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                server.start();
            }
        });

        var stopButton = new Button("stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.stop();
            }
        });

        buttons.add(startButton);
        buttons.add(stopButton);

        setLayout(new FlowLayout());
        JTextField field = new JTextField(20); // Adjust the size as needed
        add(field);

        // Arrange panels in the main frame
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(chatPanel)
                        .addComponent(buttons)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(chatPanel)
                        .addComponent(buttons)
        );

        pack();

        var messagesSize = server.Chat.size();

        server.Chat.forEach(m -> chatArea.append(m.Username + ": " + m.Content + "\n"));

        server.onNewMessage = this::appendMessage;
    }

    private void appendMessage(String message) {
        chatArea.append(message + "\n");
    }
}
