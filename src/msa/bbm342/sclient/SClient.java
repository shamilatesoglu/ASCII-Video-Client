package msa.bbm342.sclient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SClient {
    private static final int PORT = 4242;

    private JTextArea frame;
    private JPanel mainPanel;
    private JButton connectButton;
    private JButton closeConnectionButton;

    private boolean isConnected;

    private int resolutionX;
    private int resolutionY;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("SClient");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frame.setContentPane(new SClient().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public SClient() throws IOException {
        closeConnectionButton.setEnabled(false);
        connectButton.setEnabled(true);

        closeConnectionButton.addActionListener(e -> disconnect());

        connectButton.addActionListener(e -> connect());
    }

    private void disconnect() {
        new Thread(() -> {
            isConnected = false;
        }).start();
    }

    private void connect() {
        isConnected = true;
        closeConnectionButton.setEnabled(true);
        connectButton.setEnabled(false);
        new Thread(() -> {
            try {
                Integer numFrames = null;
                int i = 0;
                while (isConnected && (i < (numFrames == null ? 1 : numFrames))) {
                    Socket socket = new Socket("localhost", PORT);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    if (numFrames == null) {
                        out.println("FORMAT");
                        String response = in.readLine();
                        System.out.println("Message received: " + response);
                        Scanner scanner = new Scanner(response);
                        String resolution = scanner.next();
                        resolutionX =Integer.parseInt( resolution.split("x")[0]);
                        resolutionY =Integer.parseInt( resolution.split("x")[1]);
                        numFrames = scanner.nextInt();
                    } else {
                        out.println("GET " + i++);
                        StringBuilder frameStr = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            frameStr.append(line);
                            frameStr.append("\n");
                        }
                        frame.setText(frameStr.toString());
                    }

                    in.close();
                    out.close();
                    socket.close();
                }
                closeConnectionButton.setEnabled(false);
                connectButton.setEnabled(true);

            } catch (Exception e) {
                e.printStackTrace();
                isConnected = false;
                closeConnectionButton.setEnabled(false);
                connectButton.setEnabled(true);
            }
        }).start();
    }
}
