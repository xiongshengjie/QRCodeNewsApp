package cn.xcloude.qrcodenewsapp.utils;

import android.support.annotation.Nullable;

import java.io.IOException;

import cn.xcloude.qrcodenewsapp.interfaces.ProgressListener;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Administrator on 2018/2/21.
 */

public class ProgressRequestBody extends RequestBody {

    //实际的待包装请求体
    private RequestBody requestBody;
    //进度回调接口
    private ProgressListener progressListener;
    //包装完成的BufferedSink
    private BufferedSink bufferedSink;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;

    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }

        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }


    /**
     * 写入，回调进度接口
     *
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;

                progressListener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
            }
        };
    }
}
