package com.gonzalo.recordplay;

import android.util.Log;


public class EchoEffect {
	private static float tiempo=0.1f;
	private static float decay=0.3f;
	static final String LOG_TAG2 = EchoEffect.class.getSimpleName();
	
	protected EchoEffect(){}
	
	public double[] EchoEfecto(double[] audio, int audioSize, int fs){
		int M = (int) (fs*tiempo);
		Convolution conv = new Convolution();
		double[] out = new double[audioSize];
		double[] kernel= new double[M];
		for(int i=0; i<M; i++){
			kernel[i]=0;
		}
		kernel[0]=1-decay;
		kernel[M-1]=decay;
		out=conv.convolute(audio, kernel);
		return out;
	}
}