public class VideoSource {

    private final String host;
    private final Integer port;
    private final Integer channel;

    public VideoSource(String host, Integer port, Integer channel) {
        this.host = host;
        this.port = port;
        this.channel = channel;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getChannel() {
        return channel;
    }
}
