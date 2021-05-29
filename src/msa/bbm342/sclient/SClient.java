package msa.bbm342.sclient;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class SClient {
    private static final int PORT = 4242;

    private JTextArea frame;
    private JPanel mainPanel;
    private JButton playButton;
    private JButton stopButton;
    private JLabel fpsLabel;

    private long previousFrameTimestamp;

    private final Queue<Frame> frameBuffer;

    private AsciiVideoConnection videoConnection;
    private Feeder feeder;
    private Viewer viewer;
    private Thread feederThread;
    private Thread viewerThread;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("SClient");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frame.setContentPane(new SClient().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public SClient() throws IOException {
        stopButton.setEnabled(false);
        playButton.setEnabled(true);

        stopButton.addActionListener(e -> stop());
        playButton.addActionListener(e -> play());

        frameBuffer = new ArrayDeque<>();
        initializeThreads();
    }

    private void initializeThreads() {
        videoConnection = new AsciiVideoConnection(new VideoSource("localhost", PORT, 1));
        feeder = new Feeder(videoConnection, frameBuffer, () -> {viewer.notifyFeedingStopped();});
        feederThread = new Thread(feeder);


        viewer = new Viewer(frameBuffer, f -> {
            frame.setText(f.getFrame());

            // Measure real FPS
            long timestamp = System.nanoTime();
            if (previousFrameTimestamp != 0) {
                long diff = timestamp - previousFrameTimestamp;
                double sec = diff / 1000000000.0;
                fpsLabel.setText(String.format("FPS: %.2f", 1.0 / sec));
            }
            previousFrameTimestamp = timestamp;
        }, this::onStop);

        viewerThread = new Thread(viewer);
    }

    private void stop() {
        new Thread(() -> {
            feeder.stop();
            viewer.stop();
        }).start();
    }

    private void play() {
        onPlay();

        if (!feederThread.isAlive() && !viewerThread.isAlive()) {
            feeder.start();
            feederThread = new Thread(feeder);
            feederThread.start();

            viewer.play();
            viewerThread = new Thread(viewer);
            viewerThread.start();
        }

    }

    private void onPlay() {
        stopButton.setEnabled(true);
        playButton.setEnabled(false);
    }

    private void onStop() {
        stopButton.setEnabled(false);
        playButton.setEnabled(true);
        frameBuffer.clear();
    }

}
