/* Author: Gonzalo Luzardo  gonchalox@gmail.com
 * Description: Recording audio from mic and play before
 *              The recorded raw data is stored is a buffer in harddisk
 *              This buffer can be used to implement any audio effect
 *              This buffer is played later
 */

package com.gonzalo.recordplay;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class PlayRecord extends Activity {

	Thread t;
    boolean isRunning = true;
    boolean isRecording = false;
    boolean recorded = false;
    boolean isPlaying = false;
    private Button btnRecordStop;
    private Button btnPlay;
    private RadioGroup radioEffectsGroup;
    private ProgressBar progressPlay;
    private AudioRecord recorder;
    private AudioTrack audioTrack;
    private int bufferSize,bufferPlaySize;
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "buffer_data.raw";
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_PLAY_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private Thread recordingThread = null;
    private int recordedBufferSize=0;
    private int curretBufferPlaying=0;
    private short currentEffect;
    
    static final String LOG_TAG = PlayRecord.class.getSimpleName();
    static final short EFFECT_NONE = 0;
    static final short EFFECT_ECHO = 1;
    static final short EFFECT_ARD = 2;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record);
        currentEffect = EFFECT_NONE;
        //Calculate buffer size
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
        		                                  RECORDER_CHANNELS,
        		                                  RECORDER_AUDIO_ENCODING);
        Log.d(LOG_TAG,"Min Buffer Record Size: "+bufferSize+ "bytes");
        bufferPlaySize = AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE,
        										  RECORDER_PLAY_CHANNELS, 
                								  RECORDER_AUDIO_ENCODING);
        Log.d(LOG_TAG,"Min Buffer Play Size: "+bufferPlaySize);
        
        // Point widgets
        progressPlay = (ProgressBar) findViewById(R.id.progressPlay);
        btnRecordStop = (Button) findViewById(R.id.btn_record);
        btnPlay = (Button) findViewById(R.id.btn_play);
        radioEffectsGroup = (RadioGroup) findViewById(R.id.optGroupEffects);

        // Record button
        OnClickListener listenerBtnRecordStop = new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(isRecording){
					stopRecording();
				}
				else{
					startRecording();	
				}
			}
        };
        
        // Play button
        OnClickListener listenerBtnPlay= new OnClickListener(){
			@Override
			public void onClick(View v) {
				playRecord();		
			}
        };
        
        OnCheckedChangeListener listenerOptEffects = new OnCheckedChangeListener() {
        	 
    		
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				int selectedId = radioEffectsGroup.getCheckedRadioButtonId();
			     
    			// Find the radiobutton by returned id
    		    RadioButton radioBtn = (RadioButton) findViewById(selectedId);
    		    if(radioBtn.getId()==R.id.optNone){
    		    	currentEffect=EFFECT_NONE;
    		    }	
    		    else if(radioBtn.getId()==R.id.optEcho){
    		    	currentEffect=EFFECT_ECHO;
    		    }
    		    else{
    		    	currentEffect=EFFECT_ARD;
    		    }
    		    Log.d(LOG_TAG,"Current Effect: " + currentEffect);
    		}
     
    	};
        
        // Set the listeners
        btnRecordStop.setOnClickListener(listenerBtnRecordStop);
        btnPlay.setOnClickListener(listenerBtnPlay);
        radioEffectsGroup.setOnCheckedChangeListener(listenerOptEffects);
    }

    public void startRecording(){
    	recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                					RECORDER_SAMPLERATE, 
                					RECORDER_CHANNELS,
                					RECORDER_AUDIO_ENCODING, 
                					bufferSize);
    	recorder.startRecording();
    	btnRecordStop.setText(getString(R.string.btn_txt_stop));
    	btnPlay.setEnabled(false);
		recorder.startRecording();		
		isRecording=true;
		
		recordingThread = new Thread(new Runnable() {         
			@Override
			public void run() {
               writeAudioDataToFile();
			   //writeAudioToDisk();
            }
		},"AudioRecorder Thread");
		recordingThread.start();
		Log.d(LOG_TAG,"Start recording");
    }
    
    public void stopRecording(){
    	if(null != recorder){
            isRecording = false;
            recorder.stop();
            recorder.release();
            
            recorder = null;
            recordingThread = null;
            
            recorded = true;
            btnRecordStop.setText(getString(R.string.btn_txt_record));
            btnPlay.setEnabled(true);
            Log.d(LOG_TAG,"Size buffer recorded: " + recordedBufferSize);
    	}
    	//copyWaveFile(getTempFilename(),getFilename());
        //deleteTempFile();
    }
    
    public void playRecord(){
    	if(recorded){
    		// Thread to play audio
    		btnRecordStop.setEnabled(false);
            btnPlay.setEnabled(false);
            Log.d(LOG_TAG,"Calling playing task");
            new PlayTask().execute(); 
    	}
    }
    
    
    public void playBuffer(){
    	// If the audio was recorded
    	if(recorded){
    		Log.d(LOG_TAG,"Startting play buffer");
    		FileInputStream in = null;          
            try 
            {
                in = new FileInputStream(getTempFilename());              
	            if(null != in){ // Can read buffer
	            	audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
	            							RECORDER_SAMPLERATE, 
	            							RECORDER_PLAY_CHANNELS,
	            							RECORDER_AUDIO_ENCODING, 
	            							bufferPlaySize,
	            							AudioTrack.MODE_STREAM);		
	            	audioTrack.play();
	            	byte[] data = new byte[bufferSize];
	            	isPlaying=true;
	            	curretBufferPlaying=0;
	            	double progress = 0;
	    	      
	            	while(in.read(data) != -1){
	                     audioTrack.write(data,0,bufferSize);
	                     curretBufferPlaying++;
	                     progress = ((double) curretBufferPlaying/recordedBufferSize)*100.0;
	                     progressPlay.setProgress((int) progress);
	 	    			 //Log.e(LOG_TAG,"Playing " + progress);
	            	}
		            in.close();	                
		            audioTrack.stop();
		  		    audioTrack.release();
		  		    isPlaying=false;
	            }
	      	}
	        catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            Log.e(LOG_TAG,"Error reading buffer in disk");
	        } 
            catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} 
    }

	@SuppressWarnings("unused")
	private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        
        if(!file.exists()){
                file.mkdirs();
        }
        
        //return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
        return (file.getAbsolutePath() + "/" + "audio" + AUDIO_RECORDER_FILE_EXT_WAV);
        
    }
	
	private void applyEffect(){
		
	}
    
    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        
        if(!file.exists()){
                file.mkdirs();
        }
        
        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
        
        if(tempFile.exists())
                tempFile.delete();
        
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }
    
    private void writeAudioToDisk(){	
    	byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;
        
        try {
           os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
        
        int read = 0;
        recordedBufferSize = 0;
        if(null != os){
            while(isRecording){
              read = recorder.read(data, 0, bufferSize);                 
              if(AudioRecord.ERROR_INVALID_OPERATION != read){
                  try {
                    os.write(data);
                    recordedBufferSize++;
                  } 
                  catch (IOException e) {
                       e.printStackTrace();
                  }
               }
            }
            
            try {
                os.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings("unused")
	private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;
        
        try {
                os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        
        int read = 0;
        
        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);  
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            try {
                os.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings("unused")
	private void deleteTempFile() {
        File file = new File(getTempFilename());    
        file.delete();
    }

    @SuppressWarnings("unused")
	private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
        
        byte[] data = new byte[bufferSize];
        
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            
            Log.d(LOG_TAG,"File size: " + totalDataLen/1024 + " KB");
            
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                            longSampleRate, channels, byteRate);
            
            while(in.read(data) != -1){
                out.write(data);
            }
            
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
                FileOutputStream out, long totalAudioLen,
                long totalDataLen, long longSampleRate, int channels,
                long byteRate) throws IOException {
        
        byte[] header = new byte[44];
        
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.play_record, menu);
        return true;
    }

    public void onDestroy(){
          super.onDestroy();
          isRunning = false;
          try {
        	 t.join();
          }
          catch (InterruptedException e) {
             e.printStackTrace();
          }
          t = null;
     }
    
    
    private class PlayTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(LOG_TAG,"Starting playing");
			playBuffer();
			return null;
		}
	
		protected void onPostExecute(Void result){
			btnPlay.setEnabled(true);
			btnRecordStop.setEnabled(true);
		}
    }
}