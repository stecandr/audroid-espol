package com.gonzalo.recordplay;

public class EchoEffect {
	private static int tiempo=1;
	private static float decay=0.3f;
	
	
	public static double[] EchoEffect(double[] audio, int audioSize, int fs){
		int M = fs*tiempo;
		Convolution conv = new Convolution();
		double[] out = new double[audioSize];
		double[] kernel= new double[M];
		for(int i=0; i<M; i++){
			kernel[i]=0f;
		}
		kernel[0]=1-decay;
		kernel[M]=decay;
		out=conv.convolute(audio, kernel);
		return out;
	}
}