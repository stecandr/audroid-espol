package com.gonzalo.recordplay;

public class ReverbEffect {
	private static int tiempo=1;
	//private static float decay=0.3f;
	
	public static double[] ReverbEffect(double[] audio, int audioSize, int fs){
		int M = fs*tiempo;
		int d, e;
		int k=1;
		Convolution conv = new Convolution();
		double[] out = new double[audioSize];
		double[] kernel= new double[M];
		for(int i=0; i<M; i++){
			kernel[i]=0f;
		}
		
		out=conv.convolute(audio, kernel);
		return out;
	}
}
