package msa.bbm342.sclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
        out.println("FORMAT");
        String response = in.readLine();
        Scanner scanner = new Scanner(response);
        String[] resolution = scanner.next().split("x");
        int resolutionX = Integer.parseInt(resolution[0]);
        int resolutionY = Integer.parseInt(resolution[1]);
        int numberOfDistinctFrames = scanner.nextInt();
        VideoInfo videoInfo = new VideoInfo(resolutionX, resolutionY, numberOfDistinctFrames);
        stopConnection();

        return videoInfo;
    }

    public Frame getFrame(int frameIdx) throws IOException {

        startConnection();
        out.println("TIME " + frameIdx);
        int timeToDisplay = Integer.parseInt(in.readLine());
        stopConnection();

        startConnection();
        out.println("GET " + frameIdx);
        StringBuilder frame = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            frame.append(line);
            frame.append("\n");
        }
        stopConnection();

        return new Frame(timeToDisplay, frame.toString());
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
