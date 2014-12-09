package edu.gvsu.masl.echoprint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioFingerprinter implements Runnable {

	public final static String META_SCORE_KEY = "meta_score";
	public final static String SCORE_KEY = "score";
	public final static String ALBUM_KEY = "release";
	public final static String TITLE_KEY = "title";
	public final static String TRACK_ID_KEY = "id";
	public final static String ARTIST_KEY = "artist_name";
	
	private final String SERVER_URL = "http://developer.echonest.com/api/v4/song/identify?api_key=6KXUQIGM3OCEI8DSO&version=4.12&code=";
	
	private final int FREQUENCY = 11025;
	private final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
	private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;	

	private Thread thread;
	private volatile boolean isRunning = false;
	AudioRecord mRecordInstance = null;
	
	private short audioData[];
	private int bufferSize;	
	private int secondsToRecord;
	private volatile boolean continuous;
	
	private AudioFingerprinterListener listener;
	
	public AudioFingerprinter(AudioFingerprinterListener listener)
	{
		this.listener = listener;
	}
	
	public void fingerprint()
	{
		// set dafault listening time to 20 seconds
		this.fingerprint(20);
	}
	
	public void fingerprint(int seconds)
	{
		// no continuous listening
		this.fingerprint(seconds, false);
	}
	
	public void fingerprint(int seconds, boolean continuous)
	{
		if(this.isRunning)
			return;
				
		this.continuous = continuous;
		
		// cap to 30 seconds max, 10 seconds min.
		this.secondsToRecord = Math.max(Math.min(seconds, 30), 10);
		
		// start the recording thread
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() 
	{
		this.continuous = false;
		if(mRecordInstance != null)
			mRecordInstance.stop();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.isRunning = true;
		try 
		{			
			// create the audio buffer
			// get the minimum buffer size
			int minBufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);
			
			// and the actual buffer size for the audio to record
			// frequency * seconds to record.
			bufferSize = Math.max(minBufferSize, this.FREQUENCY * this.secondsToRecord);
						
			audioData = new short[bufferSize];
						
			// start recorder
			mRecordInstance = new AudioRecord(
								MediaRecorder.AudioSource.MIC,
								FREQUENCY, CHANNEL, 
								ENCODING, minBufferSize);
						
			willStartListening();
			
			mRecordInstance.startRecording();
			boolean firstRun = true;
			do 
			{		
				try
				{
					willStartListeningPass();
					
					long time = System.currentTimeMillis();
					// fill audio buffer with mic data.
					int samplesIn = 0;
					do 
					{					
						samplesIn += mRecordInstance.read(audioData, samplesIn, bufferSize - samplesIn);
						
						if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
							break;
					} 
					while (samplesIn < bufferSize);				
					Log.d("Fingerprinter", "Audio recorded: " + (System.currentTimeMillis() - time) + " millis");
										
					// see if the process was stopped.
					if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED || (!firstRun && !this.continuous))
						break;
					
					// create an echoprint codegen wrapper and get the code
					time = System.currentTimeMillis();
					Codegen codegen = new Codegen();
	    			String code = codegen.generate(audioData, samplesIn);
	    			Log.d("Fingerprinter", "Codegen created in: " + (System.currentTimeMillis() - time) + " millis");
	    			
	    			if(code.length() == 0)
	    			{
	    				// no code?
	    				// not enough audio data?
						continue;
	    			}
	    			
	    			didGenerateFingerprintCode(code);
	    			
	    			// fetch data from echonest
	    			time = System.currentTimeMillis();
	    			
	    			//code = "eJwtlNmhJSEIRFNqRQXDEZH8Q3iHO_NTdpcUyKJf1-_7uhqwL3AW4A-IgldcwsnXC-CkHaAPQNDKQCurOC3Oys7q96AVr68oLms3cTA-uNFwOnr9Sn1NvAxFMaxg14bjfly04wHzw242K2B39voSXM1RsJBNqw3-vumlKO0M4s4Kvj4Uq06_eoEUJ8jWKJhoV1VjWcFGu06ZVDXWJfiKgse2VjW08tCGVnvBWAUYKx-faplUMlqutAqrjj-N2o3y8spBAvZhZ-XKqiSG2WeViC20Vq6sSKu6WLkyx4HdMqkErSpk1S1LYJe_Xe3Zg409C9YPcLDrgNsKtn3yxCcJ9BhpL0bT6ROyfz5jxZCh1kNn7ycovDd9dnxQgph5qORpWOpj7TZ6wLNCUlpnFdlfQlZ-b0Ba65_K_oUYht1Lu6znfelPsO9u8oiH_XnMG_GwZzwW8WRJtKw2nrAcKq8xsUtELanR6fuLBylB-yBltZxncxwLU1o4LTvkl9pov_XxJO60vn2LJRNwznrjtbu5DttbpjLtR5prBmRyjsiUZhLZvEcyHNisgwHzOXOfw07rlGodfI5hMzEhEGQIOsvPu45LPLd-dwjx6L13dMSjAlfH2BVvZUNHfST8Kn2iPtxJuT6LTPmyyDh5r1xWX57SzXydOMQ7HVJMmU6jl-QWHEIbfWa2bAcJlf501fbTH9vt9Z-ea63GSqhgAOoc3Z27Tspu5zVaELeaZ7ORj7hLkf2c924adue4--_875yfnjqL_tNz_ij97_wJ-f_8MvUXn1cn5iOPOb4LKZlzQra1MgNy29xOP3gqFiRvTgikvPe9HJfpSKpJYXl5HDKnDUjpzojwcfOem5PL4pKdh6OewJttH6vXRuwunjdavJjFfYhDH-z-ARzPWEo=";
					String urlstr = SERVER_URL + code;			
					HttpClient client = new DefaultHttpClient();
	    			HttpGet get = new HttpGet(urlstr);
	    			
	    			// get response
	    			HttpResponse response = client.execute(get);                
	    			// Examine the response status
	    	        Log.d("Fingerprinter",response.getStatusLine().toString());
	
	    	        // Get hold of the response entity
	    	        HttpEntity entity = response.getEntity();
	    	        // If the response does not enclose an entity, there is no need
	    	        // to worry about connection release
	
	    	        String result = "";
	    	        if (entity != null) 
	    	        {
	    	            // A Simple JSON Response Read
	    	            InputStream instream = entity.getContent();
	    	            result= convertStreamToString(instream);
	    	            // now you have the string representation of the HTML request
	    	            instream.close();
	    	        }
	     			Log.d("Fingerprinter", "Results fetched in: " + (System.currentTimeMillis() - time) + " millis");
	    			
	     			
	    			// parse JSON
		    		JSONObject jObj = new JSONObject(result).getJSONObject("response");
		    		JSONObject statusObj = jObj.getJSONObject("status");
		    		
		    		if(statusObj.has("code"))
		    			Log.d("Fingerprinter", "Response code:" + statusObj.getInt("code") + " (" + this.messageForCode(statusObj.getInt("code")) + ")");
		    		
		    		if(jObj.has("songs"))
		    		{
		    			JSONArray songsArrayObj = jObj.getJSONArray("songs");
		    			
		    			if(!songsArrayObj.isNull(0))
		    			{
		    				JSONObject songsObj = songsArrayObj.getJSONObject(0);
		    				Hashtable<String, String> match = new Hashtable<String, String>();
		    				match.put(SCORE_KEY, songsObj.getDouble(SCORE_KEY) + "");
		    				match.put(TRACK_ID_KEY, songsObj.getString(TRACK_ID_KEY));
		    				
		    				if(songsObj.has(SCORE_KEY)) match.put(META_SCORE_KEY, songsObj.getDouble(SCORE_KEY) + "");
		    				if(songsObj.has(TITLE_KEY)) match.put(TITLE_KEY, songsObj.getString(TITLE_KEY));
		    				if(songsObj.has(ARTIST_KEY)) match.put(ARTIST_KEY, songsObj.getString(ARTIST_KEY));
		    				if(songsObj.has(ALBUM_KEY)) match.put(ALBUM_KEY, songsObj.getString(ALBUM_KEY));
		    				
		    				
		    				didFindMatchForCode(match, code);
		    			}
	    				else
	    					didNotFindMatchForCode(code);	    			
		    		}	    		
		    		else
		    		{
		    			didFailWithException(new Exception("Unknown error"));
		    		}
		    		
		    		firstRun = false;
				
		    		didFinishListeningPass();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Log.e("Fingerprinter", e.getLocalizedMessage());
					
					didFailWithException(e);
				}
			}
			while (this.continuous);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			Log.e("Fingerprinter", e.getLocalizedMessage());
			
			didFailWithException(e);
		}
		
		if(mRecordInstance != null)
		{
			mRecordInstance.stop();
			mRecordInstance.release();
			mRecordInstance = null;
		}
		this.isRunning = false;
		
		didFinishListening();
	}
	
	private static String convertStreamToString(InputStream is) 
	{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
		
	private String messageForCode(int code)
	{
		try{
			String codes[] = {
					"NOT_ENOUGH_CODE", "CANNOT_DECODE", "SINGLE_BAD_MATCH", 
					"SINGLE_GOOD_MATCH", "NO_RESULTS", "MULTIPLE_GOOD_MATCH_HISTOGRAM_INCREASED",
					"MULTIPLE_GOOD_MATCH_HISTOGRAM_DECREASED", "MULTIPLE_BAD_HISTOGRAM_MATCH", "MULTIPLE_GOOD_MATCH"
					}; 
	
			return codes[code];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return "UNKNOWN";
		}
	}
	
	private void didFinishListening()
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFinishListening();
				}
			});
		}
		else
			listener.didFinishListening();
	}
	
	private void didFinishListeningPass()
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFinishListeningPass();
				}
			});
		}
		else
			listener.didFinishListeningPass();
	}
	
	private void willStartListening()
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.willStartListening();
				}
			});
		}
		else	
			listener.willStartListening();
	}
	
	private void willStartListeningPass()
	{
		if(listener == null)
			return;
			
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.willStartListeningPass();
				}
			});
		}
		else
			listener.willStartListeningPass();
	}
	
	private void didGenerateFingerprintCode(final String code)
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didGenerateFingerprintCode(code);
				}
			});
		}
		else
			listener.didGenerateFingerprintCode(code);
	}
	
	private void didFindMatchForCode(final Hashtable<String, String> table, final String code)
	{
		if(listener == null)
			return;
			
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFindMatchForCode(table, code);
				}
			});
		}
		else
			listener.didFindMatchForCode(table, code);
	}
	
	private void didNotFindMatchForCode(final String code)
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didNotFindMatchForCode(code);
				}
			});
		}
		else
			listener.didNotFindMatchForCode(code);
	}
	
	private void didFailWithException(final Exception e)
	{
		if(listener == null)
			return;
			
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFailWithException(e);
				}
			});
		}
		else
			listener.didFailWithException(e);
	}
	
	/**
	 * Interface for the fingerprinter listener<br>
	 * Contains the different delegate methods for the fingerprinting process
	 */
	public interface AudioFingerprinterListener
	{
	    /**
	     * Called when the fingerprinter process loop has finished
	     */
	    public void didFinishListening();
	 
	    /**
	     * Called when a single fingerprinter pass has finished
	     */
	    public void didFinishListeningPass();
	 
	    /**
	     * Called when the fingerprinter is about to start
	     */
	    public void willStartListening();
	 
	    /**
	     * Called when a single listening pass is about to start
	     */
	    public void willStartListeningPass();
	 
	    /**
	     * Called when the codegen libary generates a fingerprint code
	     * @param code the generated fingerprint as a zcompressed, base64 string
	     */
	    public void didGenerateFingerprintCode(String code);
	 
	    /**
	     * Called if the server finds a match for the submitted fingerprint code
	     * @param table a hashtable with the metadata returned from the server
	     * @param code the submited fingerprint code
	     */
	    public void didFindMatchForCode(Hashtable<String, String> table, String code);
	 
	    /**
	     * Called if the server DOES NOT find a match for the submitted fingerprint code
	     * @param code the submited fingerprint code
	     */
	    public void didNotFindMatchForCode(String code);
	 
	    /**
	     * Called if there is an error / exception in the fingerprinting process
	     * @param e an exception with the error
	     */
	    public void didFailWithException(Exception e);
	}
}
