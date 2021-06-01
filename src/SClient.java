import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SClient {
    private static int sPort = 4242;
    private static String sHost = "localhost";
    private static int sChannel = 1;

    private JTextArea frame;
    private JPanel mainPanel;
    private JButton playButton;
    private JButton stopButton;
    private JLabel fpsLabel;
    private JLabel bufferHealth;
    private JSlider videoSeek;
    private JLabel frameLabel;
    private JComboBox<String> channelBox;

    private long previousFrameTimestamp;

    private final BlockingQueue<Frame> frameBuffer;

    private AsciiVideoConnection videoConnection;
    private Feeder feeder;
    private Viewer viewer;
    private Thread feederThread;
    private Thread viewerThread;
    private ChangeListener onSeek;

    private static void setupUI() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("ASCII Video Client");
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().contains("Nimbus")) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        frame.setContentPane(new SClient().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-p")) {
                sPort = Integer.parseInt(args[i + 1]);
            }
            if (arg.equals("-a")) {
                sHost = args[i + 1].trim();
            }
            if (arg.equals("-ch")) {
                sChannel = Integer.parseInt(args[i + 1]);
            }
        }

        setupUI();
    }

    public SClient() throws IOException {
        stopButton.setEnabled(false);
        playButton.setEnabled(true);

        channelBox.setSelectedItem(String.valueOf(sChannel));

        frameBuffer = new ArrayBlockingQueue<>(Feeder.BUFFER_SIZE);

        initializeThreads();

        initializeListeners();
    }

    private void initializeListeners() {

        stopButton.addActionListener(e -> onStopButtonClicked());
        playButton.addActionListener(e -> onPlayButtonClicked());

        channelBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sChannel = Integer.parseInt(e.getItem().toString());
                onStopButtonClicked();
                initializeThreads();
            }
        });
    }

    private void initializeThreads() {
        videoConnection = new AsciiVideoConnection(new VideoSource(sHost, sPort, sChannel));

        if (viewerThread != null) {
            viewerThread.interrupt();
            viewerThread = null;
        }
        if (feederThread != null) {
            feederThread.interrupt();
            feederThread = null;
        }

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Get video info and frame times
                    VideoInfo videoInfo = videoConnection.getVideoInfo();

                    videoSeek.setMaximum(videoInfo.getFrameCount());

                    // Create feeder thread
                    feeder = new Feeder(videoConnection, videoInfo, frameBuffer, () -> {
                        viewer.notifyFeedingStopped();
                    });
                    feederThread = new Thread(feeder);

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

                        frameLabel.setText(viewer.getCurrentFrameIdx() + " / " + videoSeek.getMaximum());
                    }, () -> onStopButtonClicked());

                    viewerThread = new Thread(viewer);

                    onSeek = e -> {
                        if (Math.abs(viewer.getCurrentFrameIdx() - videoSeek.getValue()) > 1) {
                            feeder.setCurrentFrameIdx(videoSeek.getValue());
                            viewer.setCurrentFrameIdx(videoSeek.getValue());
                        }
                    };
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void onStopButtonClicked() {
        stopButton.setEnabled(false);
        playButton.setEnabled(true);
        feeder.stop();
        viewer.stop();
        videoSeek.removeChangeListener(onSeek);
    }

    private void onPlayButtonClicked() {
        frameBuffer.clear();

        videoSeek.addChangeListener(onSeek);
        videoSeek.setValue(0);

        stopButton.setEnabled(true);
        playButton.setEnabled(false);

        feeder.start();
        feederThread = new Thread(feeder);
        feederThread.start();

        viewer.play();
        viewerThread = new Thread(viewer);
        viewerThread.start();
    }

}
