package msa.bbm342.sclient;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

public class SClient {
    private static final int PORT = 4242;
    private static final String HOST = "localhost";
    private int channel = 1;

    private JTextArea frame;
    private JPanel mainPanel;
    private JButton playButton;
    private JButton stopButton;
    private JLabel fpsLabel;
    private JLabel bufferHealth;
    private JSlider videoSeek;
    private JLabel frameLabel;
    private JComboBox channelBox;

    private long previousFrameTimestamp;

    private final Queue<Frame> frameBuffer;

    private AsciiVideoConnection videoConnection;
    private Feeder feeder;
    private Viewer viewer;
    private Thread feederThread;
    private Thread viewerThread;
    private ChangeListener onVideoSeek;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("ASCII Video");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frame.setContentPane(new SClient().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public SClient() throws IOException {
        stopButton.setEnabled(false);
        playButton.setEnabled(true);

        frameBuffer = new ArrayDeque<>();

        initializeThreads();

        initializeListeners();
    }

    private void initializeListeners() {

        stopButton.addActionListener(e -> stop());
        playButton.addActionListener(e -> play());

        channelBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + e.getItem());
                channel = Integer.parseInt(e.getItem().toString());
                stop();
                initializeThreads();
            }
        });
    }

    private void initializeThreads() {
        videoConnection = new AsciiVideoConnection(new VideoSource("localhost", PORT, channel));

        new Thread(() -> {
            try {
                // Get video info and frame times
                VideoInfo videoInfo = videoConnection.getVideoInfo();

                // Create feeder thread
                feeder = new Feeder(videoConnection, videoInfo, frameBuffer, () -> {
                    viewer.notifyFeedingStopped();
                });
                feederThread = new Thread(feeder);

                videoSeek.setMaximum(videoInfo.getFrameCount());

                // Create viewer thread
                viewer = new Viewer(frameBuffer, dequeuedFrame -> {
                    frame.setText(dequeuedFrame.getFrame());

                    // Measure real FPS
                    long timestamp = System.nanoTime();
                    if (previousFrameTimestamp != 0) {
                        long diff = timestamp - previousFrameTimestamp;
                        double sec = diff / 1000000000.0;
                        fpsLabel.setText(String.format("FPS: %.2f", 1.0 / sec));
                    }
                    previousFrameTimestamp = timestamp;

                    // Print buffer health:
                    int bufferedFrameCount = frameBuffer.size();
                    bufferHealth.setText("Buffer Health: " + (bufferedFrameCount / Viewer.FPS) + "s (" + bufferedFrameCount + " frames)");

                    // Set seek
                    if (videoSeek.getValue() == viewer.getCurrentFrameIdx() - 1) {
                        videoSeek.setValue(viewer.getCurrentFrameIdx());
                    } else {
                        frameBuffer.clear();
                    }

                    frameLabel.setText("Frame: " + viewer.getCurrentFrameIdx() + " / " + videoSeek.getMaximum());

                }, this::onStop);

                viewerThread = new Thread(viewer);

                onVideoSeek = e -> {
                    if (Math.abs(viewer.getCurrentFrameIdx() - videoSeek.getValue()) > 1) {
                        feeder.setCurrentFrameIdx(videoSeek.getValue());
                        viewer.setCurrentFrameIdx(videoSeek.getValue());
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void stop() {
        new Thread(() -> {
            feeder.stop();
            viewer.stop();
            videoSeek.removeChangeListener(onVideoSeek);
            videoSeek.setValue(0);
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

        videoSeek.addChangeListener(onVideoSeek);

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
