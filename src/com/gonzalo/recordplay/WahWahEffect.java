package com.gonzalo.recordplay;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WahWahEffect {
	
	/** Creates a wahwah'd version of the PCM data.
	 * @param sourceData The 16-bit stereo PCM data
	 * @param sampleRate The sample rate of the PCM data
	 * @return The wahwah'd PCM data
	 */
	
	protected WahWahEffect(){}
	
	public static ByteBuffer createWahData(ByteBuffer sourceData, int sampleRate) {
	   ByteBuffer result=ByteBuffer.allocateDirect(sourceData.capacity());
	   result.order(ByteOrder.nativeOrder());

	   // Create the separate channels
	   /*ByteBuffer leftChannelBytes = BufferUtils.createByteBuffer(sourceData.limit() / 2);
	   ByteBuffer rightChannelBytes = BufferUtils.createByteBuffer(sourceData.limit() / 2);
	   for (int x = 0; x < sourceData.limit() - 2; x += 2) {
	      leftChannelBytes.put(sourceData.get(x));
	      rightChannelBytes.put(sourceData.get(x + 1));
	   }
	   leftChannelBytes.rewind();
	   rightChannelBytes.rewind();*/

	   // BEGIN WahWah.cpp ADAPTION
	   
	   // #define lfoskupsamplse = 30
	   int lfoskipsamples = 30;
	   // EffectWahwah::EffectWahwah()
	   float freq = 1.5f;
	   float depth = 0.7f;
	   float res = 2.5f;
	   float freqofs = 0.3f;
	   // EffectWahwah::NewTrackSimpleMono()
	   double lfoskip = (freq * 2 * Math.PI / (double) sampleRate);
	   int skipcount = 0;
	   double xn1 = 0, xn2 = 0, yn1 = 0, yn2 = 0;
	   double b0 = 0.0, b1 = 0.0, b2 = 0.0;
	   double a0 = 0.0, a1 = 0.0, a2 = 0.0;
	   // EffectWahwah::ProcessSimpleMono()
	   double frequency, omega, sn, cs, alpha;
	   double in, out;

	   // We are currently just messing with the left channel.
	   for (int x = 0; x < sourceData.limit(); x += 2) {
	      in = sourceData.getShort(x);

	      if ((skipcount++) % lfoskipsamples == 0) {
	         frequency = (1 + Math.cos(skipcount * lfoskip)) / 2;
	         frequency = frequency * depth * (1 - freqofs) + freqofs;
	         frequency = Math.exp((frequency - 1) * 6);
	         omega = Math.PI * frequency;
	         sn = Math.sin(omega);
	         cs = Math.cos(omega);
	         alpha = sn / (2 * res);
	         b0 = (1 - cs) / 2;
	         b1 = 1 - cs;
	         b2 = (1 - cs) / 2;
	         a0 = 1 + alpha;
	         a1 = -2 * cs;
	         a2 = 1 - alpha;
	      }
	      out = (b0 * in + b1 * xn1 + b2 * xn2 - a1 * yn1 - a2 * yn2) / a0;
	      xn2 = xn1;
	      xn1 = in;
	      yn2 = yn1;
	      yn1 = out;

	      // Prevents clipping
	      if (out < -32768.0)
	         out = -32768.0;
	      else if (out > 32767.0)
	         out = 32767.0;

	      sourceData.putShort(x, (short) (out));
	   }

	   // END WahWah.cpp ADAPTION

	   // Create the final ByteBuffer
	   for (int x = 0; x < sourceData.capacity(); x++) {
	      result.put(sourceData.get(x));
	   }
	   result.rewind();
	   return result;
	}

}
