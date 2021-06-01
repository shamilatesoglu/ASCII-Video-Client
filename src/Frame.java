public class Frame {
    private final String frame;
    private final int compressedFrameIdx;

    public Frame(String frame, int compressedFrameIdx) {
        this.frame = frame;
        this.compressedFrameIdx = compressedFrameIdx;
    }

    public String getFrame() {
        return frame;
    }

    public int getCompressedFrameIdx() {
        return compressedFrameIdx;
    }

    @Override
    protected Frame clone() throws CloneNotSupportedException {
        return (Frame) super.clone();
    }
}
