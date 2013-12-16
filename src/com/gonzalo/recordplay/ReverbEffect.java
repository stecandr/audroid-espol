package com.gonzalo.recordplay;

public class ReverbEffect {
	private static int tiempo=1;
	//private static float decay=0.3f;
	
	public static double[] ReverbEffect(double[] audio, int audioSize, int fs){
		int M = fs*tiempo;
		int d, e, k=1;
		Convolution conv = new Convolution();
		double[] out = new double[audioSize];
		double [] midaudio= new double[M+audioSize];
		double[] kernel= new double[M];
		double [] ceros= new double[M];
		for(int i=0; i<M; i++){
			ceros[i]=0f;
		}
		for(int i=0; i<M; i++){
			kernel[i]=0f;
		}
		d=(int)fs/10;
		e=(int)M/d;
		
		for(int i=0; i<M; i=i+d){
			kernel[i]=k;
			k=k-(1/e);
		}
		
		System.arraycopy(ceros, 0, midaudio, 0, M);
		System.arraycopy(audio, 0, midaudio, M, audioSize);
		
		out=conv.convolute(midaudio, kernel);
		return out;
	}
}
