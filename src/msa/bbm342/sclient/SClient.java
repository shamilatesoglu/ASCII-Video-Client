package msa.bbm342.sclient;

import javax.swing.*;
import java.io.IOException;
import java.util.Date;

public class SClient {
    private static final int PORT = 4242;

    private JTextArea frame;
    private JPanel mainPanel;
    private JButton connectButton;
    private JButton closeConnectionButton;
    private JLabel fpsLabel;

    private long previousFrameTimestamp;

    private ASCIIVideoClient videoClient;

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

        videoClient = new ASCIIVideoClient("localhost", PORT);
    }

    private void disconnect() {
        new Thread(() -> {
            videoClient.stopStreaming();
        }).start();
    }

    private void connect() {
        closeConnectionButton.setEnabled(true);
        connectButton.setEnabled(false);
        new Thread(() -> {
            try {
                videoClient.stream(s -> {
                    frame.setText(s);
                    long timestamp = System.nanoTime();
                    if (previousFrameTimestamp != 0) {
                        long diff = timestamp - previousFrameTimestamp;
                        double sec = diff / 1000000000.0;
                        fpsLabel.setText(String.format("FPS: %.2f", 1.0 / sec));
                    }
                    previousFrameTimestamp = timestamp;
                });
                closeConnectionButton.setEnabled(false);
                connectButton.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
                videoClient.stopStreaming();
                closeConnectionButton.setEnabled(false);
                connectButton.setEnabled(true);
            }
        }).start();
    }
}
