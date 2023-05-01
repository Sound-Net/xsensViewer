package xsens;

/**
 * Identifier for data contained in an MTData packet. 
 * @author Jamie Macaulay
 *
 */
public enum XsDataIdentifier {
	
	
	// XDI is a flag for the data a message contains. 
	 XDI_PacketCounter ( 0x1020),
	 XDI_SampleTimeFine ( 0x1060),
	 XDI_Quaternion ( 0x2010),
	 XDI_DeltaV ( 0x4010),
	 XDI_Acceleration ( 0x4020),
	 XDI_RateOfTurn ( 0x8020),
	 XDI_DeltaQ ( 0x8030),
	 XDI_MagneticField ( 0xC020),
	 XDI_StatusWord ( 0xE020),
	 XDI_EulerAngles ( 0x2030),
	 XDI_Temperature ( 0x2040), // 2064 in decimal - temperature data
	 XDI_Pressure ( 0x2050), // 2080 in decimal - pressure data
	 XDI_RGB ( 0x2060), // 2096 in decimal- red, green, blue data
	 XDI_BAT ( 0x2070), 	// 2112 in decimal- battery data
	 XDI_BATV ( 0x2080), 
	 XDI_LightSpectrum (0x2090),
	 XDI_SDCardUsed    (0x3000);

	
	private int value;
	
	/**
	 * The frequency of the data output. 
	 */
	private Float frequency = null; 

	/**
	 * Get the frequency in samples per second of the ouput data. 
	 * @return the frequency in samples per second. 
	 */
	public Float getFrequency() {
		return frequency;
	}

	public void setFrequency(Float frequency) {
		this.frequency = frequency;
	}



	XsDataIdentifier(int value){
		this.value=value; 
	}
	
	
	
	public int getValue() {
		return value; 
	}

}
