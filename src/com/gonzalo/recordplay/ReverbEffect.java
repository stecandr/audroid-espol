package com.gonzalo.recordplay;

import android.util.Log;

public class ReverbEffect {
	private static float tiempo=0.3f;
	//private static float decay=0.3f;
	static final String LOG_TAG3 = ReverbEffect.class.getSimpleName();
	
	protected ReverbEffect(){}
	
	public double[] ReverbEfecto(double[] audio, int audioSize, int fs){
		int M = (int) (fs*tiempo);
		int d, e, k=1;
		Convolution conv = new Convolution();
		double [] midaudio= new double[M+audioSize+1];
		double[] out = new double[audioSize];
		double[] kernel= new double[M];
		double [] ceros= new double[M];
		for(int i=0; i<M; i++){
			ceros[i]=0;
		}
		for(int j=0; j<M; j++){
			kernel[j]=0;
		}
		d=(int)fs/10;
		e=(int)M/d;
		
		for(int i=0; i<M; i=i+d){
			kernel[i]=k;
			k=k-(1/e);
		}
		/*System.arraycopy(ceros, 0, midaudio, 0, M);
		Log.d(LOG_TAG3,"aqui!");
		System.arraycopy(audio, 0, midaudio, M, audioSize);*/
		out=conv.convolute(audio, kernel);
		return out;
	}
}
