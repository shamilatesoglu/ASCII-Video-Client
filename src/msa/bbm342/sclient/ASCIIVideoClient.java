package msa.bbm342.sclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class ASCIIVideoClient {
    private static final int NOT_SET = -1;
    private static final int FPS = 20;

    private boolean isStreaming;
    private int resolutionX;
    private int resolutionY;

    private String host;
    private int port;

    public ASCIIVideoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void stream(Consumer<String> frameConsumer) throws IOException, InterruptedException {
        isStreaming = true;
        int frameIdx = 0;
        int timeToDisplay = NOT_SET;
        int numberOfFrames = NOT_SET;
        while (isStreaming && (frameIdx < (numberOfFrames == NOT_SET ? 1 : numberOfFrames))) {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (numberOfFrames == NOT_SET) {
                out.println("FORMAT");
                String response = in.readLine();
                System.out.println("Message received: " + response);
                Scanner scanner = new Scanner(response);
                String resolution = scanner.next();
                resolutionX = Integer.parseInt(resolution.split("x")[0]);
                resolutionY = Integer.parseInt(resolution.split("x")[1]);
                numberOfFrames = scanner.nextInt();
            } else if (timeToDisplay == NOT_SET) {
                out.println("TIME " + frameIdx++);
                timeToDisplay = Integer.parseInt(in.readLine());
            } else {
                out.println("GET " + frameIdx);
                StringBuilder frame = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    frame.append(line);
                    frame.append("\n");
                }
                new Thread(() -> {
                    frameConsumer.accept(frame.toString());
                }).start();

                Thread.sleep((1000L / FPS) * timeToDisplay);
                timeToDisplay = NOT_SET;
            }

            in.close();
            out.close();
            socket.close();
        }
        stopStreaming();
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void stopStreaming() {
        isStreaming = false;
    }
}
