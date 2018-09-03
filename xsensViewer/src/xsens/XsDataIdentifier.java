package xsens;

/**
 * Identifier for data contained in an MTData packaet. 
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
	 XDI_BAT ( 0x2070); 	// 2112 in decimal- battery data
	
	private int value;

	XsDataIdentifier(int value){
		this.value=value; 
	}
	
	public int getValue() {
		return value; 
	}

}
