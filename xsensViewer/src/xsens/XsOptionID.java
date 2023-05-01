package xsens;

/**
 * Options for setting xsens parameters - see Table 8 in xsens low level communication 
 * protocal
 * @author Jamie Macaulay
 *
 */
public enum XsOptionID {
	
	DisableAutoStore 			(0x00000001),
	DisableAutoMeasurement 		(0x00000002),
	EnableBeidou 				(0x00000004),
	Reserved 					(0x00000008),
	EnableAhs 					(0x00000010),
	EnableOrientationSmoother 	(0x00000020),
	EnableConfigurableBusId 	(0x00000040),
	EnableInRunCompassCalibration (0x00000080),
	EnableConfigMessageAtStartup (0x00000200),
	EnableColdFilterResets 		(0x00000400),
	EnablePositionVelocitySmoother 	(0x00000800),
	EnableContinuousZRU 		(0x00001000);

	
	/**
	 * The unsigned byte value represented as an int. 
	 */
	private int optionVal; 

	XsOptionID(int optionVal) {
		this. optionVal= optionVal;
	}

	/**
	 * Get the unsigned byte value represented as an int. 
	 * @return the unisgned byte value
	 */
	public int getValue() {
		return optionVal; 
	}

//	/**
//	 * Get the signed byte value
//	 * @return the signed byte value. 
//	 */
//	public byte getSignedByte() {
//		return (byte) ((optionVal << 24) >> 24); 
//	}

}
