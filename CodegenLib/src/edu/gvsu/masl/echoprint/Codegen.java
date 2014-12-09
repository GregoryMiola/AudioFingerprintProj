package edu.gvsu.masl.echoprint;

public class Codegen {
	private final float normalizingValue = Short.MAX_VALUE;
	native String codegen(float data[], int numSamples);
	
	static
	{
	    System.loadLibrary("echoprint-jni");
	}
	
	public String generate(float data[], int numSamples)
	{
	    return codegen(data, numSamples);
	}
	
	public String generate(short data[], int numSamples)
	{
	    float normalizeAudioData[] = new float[numSamples];
	    for (int i = 0; i < numSamples - 1; i++)
	        normalizeAudioData[i] = data[i] / normalizingValue;
	 
	    return this.codegen(normalizeAudioData, numSamples);
	}
}