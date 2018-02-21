package cn.xcloude.qrcodenewsapp.entity;

/**
 * Created by Administrator on 2018/2/21.
 */

public class ProgressModel {

    private long bytesWritten;
    private long contentLength;
    private boolean done;

    public ProgressModel(long bytesWritten, long contentLength, boolean done) {
        this.bytesWritten = bytesWritten;
        this.contentLength = contentLength;
        this.done = done;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
