package msa.bbm342.sclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class AsciiVideoConnection {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final VideoSource videoSource;

    public AsciiVideoConnection(VideoSource videoSource) {
        this.videoSource = videoSource;
    }

    public VideoInfo getVideoInfo() throws IOException {

        startConnection();
        out.println("INFO");
        String format = in.readLine();
        Scanner scanner = new Scanner(format);
        String[] resolution = scanner.next().split("x");
        int resolutionX = Integer.parseInt(resolution[0]);
        int resolutionY = Integer.parseInt(resolution[1]);
        int numberOfDistinctFrames = scanner.nextInt();

        Map<Integer, Integer> timeMap = new LinkedHashMap<>();
        Map<Integer, Integer> uncompressed = new LinkedHashMap<>();
        int frameIdx = 0;
        for (int compressedFrameIdx = 0; compressedFrameIdx < numberOfDistinctFrames; compressedFrameIdx++) {
            String line = in.readLine();
            int timeToDisplay = new Scanner(line).nextInt();
            timeMap.put(compressedFrameIdx, timeToDisplay);

            while (timeToDisplay-- > 0) {
                uncompressed.put(frameIdx++, compressedFrameIdx);
            }
        }

        VideoInfo videoInfo = new VideoInfo(resolutionX, resolutionY, uncompressed, timeMap);
        stopConnection();

        return videoInfo;
    }

    public Frame getFrame(int compressedFrameIdx) throws IOException {

        startConnection();
        out.println("GET " + compressedFrameIdx);
        StringBuilder frame = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            frame.append(line);
            frame.append("\n");
        }
        stopConnection();

        return new Frame(frame.toString(), compressedFrameIdx);
    }

    public void startConnection() throws IOException {
        socket = new Socket(videoSource.getHost(), videoSource.getPort());
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
