package xsens;

/**
 * Identifier for data contained inxsens messaging protocal i.e. the mid values in xsens messages. 
 * 
 * @author Jamie Macaulay
 *
 */
public enum XsMessageID  {

	//message types. Unlike C we keep this as int and convert back to unsigned bytes later. 
	XMID_Wakeup (0x3E),
	XMID_WakeupAck ( 0x3F),
	XMID_ReqDid ( 0x00),
	XMID_DeviceId ( 0x01),
	XMID_GotoConfig ( 0x30),
	XMID_GotoConfigAck ( 0x31),
	XMID_GotoMeasurement ( 0x10),
	XMID_GotoMeasurementAck ( 0x11),
	XMID_MtData2 ( 0x36),
	XMID_ReqOutputConfig ( 0xC0),
	XMID_SetOutputConfig ( 0xC0),
	XMID_OutputConfig ( 0xC1),
	XMID_Reset ( 0x40),
	XMID_ResetAck ( 0x41),
	XMID_Error ( 0x42),
	XMID_ReqPeriodAck ( 0x05),
	XMID_ReqPeriod ( 0x04),
	XMID_ReqSTMessage ( 0xD5),
	XMID_ReqSTMessageAck ( 0xD4),
	XMID_SetNoRotation ( 0x22),
	XMID_ReqFilterProfile (0x64),
	XMID_ReqFilterProfileAck (0x65), 
	XMID_SetRTCTime (0x66), 
	XMID_ReqRTCTime (0x67), 
	XMID_ReqSDFormat(0x68), 
	XMID_ReqSDSize(0x69),
	XMID_ReqDeviceType (0x6A),
	XMID_DeviceType(0x6B),
	XMID_ReqFirmwareVersion (0x6C),
	XMID_FirmwareVersion (0x6D),
	XMID_SetOptionFlags (0x48),
	XMID_SetOptionFlagAck (0x49),
	XMID_ReqOptionFlags (0x48),
	XMID_ReqOptionFlagsAck (0x49);



	/**
	 * The unsigned byte value represented as an int. 
	 */
	private int bytevalue; 

	XsMessageID(int bytevalue) {
		this.bytevalue=bytevalue;
	}

	/**
	 * Get the unsigned byte value represented as an int. 
	 * @return the unisgned byte value
	 */
	public int getValue() {
		return bytevalue; 
	}

	/**
	 * Get the signed byte value
	 * @return the signed byte value. 
	 */
	public byte getSignedByte() {
		return (byte) ((bytevalue << 24) >> 24); 
	}

	/**
	 * 
	 * Get the XsMessageID enum from an int. 
	 * @param ID -  the XsMessageID identifier. 
	 * @return the corresponding XsMessageID enum. 
	 */
	public static XsMessageID getXsMessageID(int ID) {
		XsMessageID[] values = XsMessageID.values();
		for (XsMessageID aXSMessageID : values) {
			if (aXSMessageID.getValue() == ID) {
				return aXSMessageID; 
			}; 
		}
		return null; 
	}

}