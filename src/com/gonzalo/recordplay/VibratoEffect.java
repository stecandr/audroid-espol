package com.gonzalo.recordplay;

import android.util.Log;
import java.math.*;



public final class VibratoEffect {
	
	static final String LOG_TAG = VibratoEffect.class.getSimpleName();


	int Modfreq = 10; //10 Khz
	double Width = 0.0008; // 0.8 Milliseconds
/*
	public double[] VibratoEfecto(int[] audio, int sampleRate){
		int ya_alt=0;
		double Delay=Width; // basic delay of input sample in sec
		double RDELAY=Math.round(Delay*sampleRate); // basic delay in # samples
		double RWIDTH=Math.round(Width*sampleRate); // modulation width in # samples
		if (RWIDTH>RDELAY){ 
		  Log.d(LOG_TAG,"delay greater than basic delay !!!");
		  return null;
		}
		float MODFREQ=Modfreq/sampleRate; // modulation frequency in # samples
		int LEN=audio.length;        // # of samples in WAV-file
		double L=2+RDELAY+RWIDTH*2; 
		
		
		int[] Delayline=zeros(L,1); // memory allocation for delay
		int[] inry=zeros(audio.length,1);     // memory allocation for output vector

		int[] dl;
		double[] y;
		double M,MOD,ZEIGER,frac,i;
		
		
		for (int n=1; n<(LEN-1);n++){
		   M=MODFREQ;
		   MOD=Math.sin(M*2*Math.PI*n);
		   ZEIGER=1+RDELAY+RWIDTH*MOD;
		   i=Math.floor(ZEIGER);
		   frac=ZEIGER-i;
		   
		   for (int j=0; j<LEN-1; j++){
			   dl[j] =audio[n]; 
			   dl[j+1]=Delayline[j];
		   }		   
		
		   Math.round(i);
		   //---Linear Interpolation-----------------------------
		   y[n]= dl[i+1] * frac + dl[i] * (1-frac); 

		   
		   //---Allpass Interpolation------------------------------
		   //y(n,1)=(Delayline(i+1)+(1-frac)*Delayline(i)-(1-frac)*ya_alt);  
		   //ya_alt=ya(n,1);
		}
	}*/
}