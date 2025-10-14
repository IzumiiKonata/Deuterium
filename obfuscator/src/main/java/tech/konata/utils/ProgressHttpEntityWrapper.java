package tech.konata.utils;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * HttpEntityWrapper with a progress callback
 *
 * @see <a href="http://stackoverflow.com/a/7319110/268795">http://stackoverflow.com/a/7319110/268795</a>
 */

public class ProgressHttpEntityWrapper extends HttpEntityWrapper {

    private final ProgressCallback progressCallback;

    public static interface ProgressCallback {
        public void progress(float progress);
    }

    public ProgressHttpEntityWrapper(final HttpEntity entity, final ProgressCallback progressCallback) {
        super(entity);
        this.progressCallback = progressCallback;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        this.wrappedEntity.writeTo(out instanceof ProgressFilterOutputStream ? out : new ProgressFilterOutputStream(out, this.progressCallback, getContentLength()));
    }

    static class ProgressFilterOutputStream extends FilterOutputStream {

        private final ProgressCallback progressCallback;
        private long transferred;
        private long totalBytes;

        ProgressFilterOutputStream(final OutputStream out, final ProgressCallback progressCallback, final long totalBytes) {
            super(out);
            this.progressCallback = progressCallback;
            this.transferred = 0;
            this.totalBytes = totalBytes;
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            //super.write(byte b[], int off, int len) calls write(int b)
            out.write(b, off, len);
            this.transferred += len;
            this.progressCallback.progress(getCurrentProgress());
        }

        @Override
        public void write(final int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.progressCallback.progress(getCurrentProgress());
        }

        private float getCurrentProgress() {
            return ((float) this.transferred / this.totalBytes) * 100;
        }

    }

}