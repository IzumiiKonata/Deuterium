package processing.sound;

import com.jsyn.data.FloatSample;
import com.jsyn.ports.QueueDataEvent;
import com.jsyn.ports.UnitDataQueueCallback;
import com.jsyn.unitgen.FixedRateStereoWriterToMono;
import com.tagtraum.jipes.math.FFTFactory;

import java.util.Arrays;

/**
 * This class copies all input to an audio buffer of the given size and performs
 * an FFT on it when required.
 */
public class JSynFFT extends FixedRateStereoWriterToMono {

	private FloatSample buffer;
	private float[] real, imaginary;
	private float[] realOut, imagOut;
	private float[] fftOut;
	FFTFactory.JavaFFT fft;

	public final int bufferSize;

	public static final int FFT_SIZE = 4096;

	public interface FFTCalcCallback {
		void onFFT(float[] fftData);
	}

	final FFTCalcCallback callback;

	protected JSynFFT(int bufferSize, FFTCalcCallback callback) {
		super();
		this.callback = callback;
		this.bufferSize = bufferSize;
		this.buffer = new FloatSample(bufferSize);
		this.real = new float[FFT_SIZE];
		this.imaginary = new float[FFT_SIZE];
		this.realOut = new float[FFT_SIZE];
		this.imagOut = new float[FFT_SIZE];
		this.fftOut = new float[FFT_SIZE];

		this.fft = new FFTFactory.JavaFFT(FFT_SIZE);

		// write any connected input into the output buffer ad infinitum

		this.dataQueue.queueWithCallback(this.buffer, new UnitDataQueueCallback() {
			@Override
			public void started(QueueDataEvent event) {

			}

			@Override
			public void looped(QueueDataEvent event) {

			}

			@Override
			public void finished(QueueDataEvent event) {

				// 左移现有数据，为新数据腾出空间
				System.arraycopy(real, bufferSize, real, 0, FFT_SIZE - bufferSize);

				// 从音频缓冲区读取新数据并添加到 real 数组尾部
				float[] bufferData = buffer.getBuffer();
				System.arraycopy(bufferData, 0, real, FFT_SIZE - bufferSize, bufferSize);

				// 清空虚部
				Arrays.fill(imaginary, 0.0f);

				// 执行 FFT
				fft.transform(false, real, imaginary, realOut, imagOut);

				// 计算频率点幅值
				for (int i = 0; i < FFT_SIZE; i++) {
					fftOut[i] = (float) Math.hypot(realOut[i] / FFT_SIZE, imagOut[i] / FFT_SIZE);
				}

				callback.onFFT(fftOut);

				// 重新排队处理下一个缓冲区
				dataQueue.queueWithCallback(buffer, this);
			}
		});
	}

	protected float[] calculateMagnitudes() {
		return this.fftOut;
	}
}