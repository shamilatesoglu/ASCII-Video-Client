package msa.bbm342.sclient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SClient {
    private JTextArea frame;
    private JPanel mainPanel;
    private JButton connectButton;
    private JButton closeConnectionButton;

    private ServerSocket listener;

    private boolean isConnected;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("SClient");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frame.setContentPane(new SClient().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public SClient() throws IOException {

        listener = new ServerSocket(9999);

        closeConnectionButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });

        connectButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
    }

    private void disconnect() {
        new Thread(() -> {
            isConnected = false;
        }).start();
    }

    private void connect() {
        new Thread(() -> {
            try {
                while (isConnected) {
                    Socket socket = listener.accept();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
