package xsens;


/**
 * Class which holds for XSens sensors
 * @author Jamie Macaulay 
 *
 */
public class XMIDOptions {
	
	
	/**
	 * The angle output type
	 */
	public XsDataIdentifier angleOuput = null; 

	/**
	 * The sample rate
	 */
	public Float sampleRate = null; 

	
	/**
	 * The in compass calibration flag
	 */
	public Boolean inCompassCal = null; 
	
	

	/**
	 * 
	 * @return true if the paramters are null 
	 */
	public boolean areParamsNull() {
		if (angleOuput == null) return true; 
		if (inCompassCal == null) return true; 
		if (sampleRate == null) return true; 
		return false;
	}

}
