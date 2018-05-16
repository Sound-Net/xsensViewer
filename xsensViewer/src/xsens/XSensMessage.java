package xsens;

/**
 * Set of messages which can be sent ot the xsens sensor. 
 * @author Jamie Macaulay
 *
 */
public class XSensMessage {
	
	
	/*! \brief Xbus message preamble byte. */
	public static int XBUS_PREAMBLE = 0xFA;
	/*! \brief Xbus message bus ID for master devices. */
	public static int XBUS_MASTERDEVICE = 0xFF;
	/*! \brief Xbus length byte for message with an extended payload. */
	public static int XBUS_EXTENDED_LENGTH = 0xFF;
	
	//message types. Unlike C we keep this as int and convert back to unsigned bytes later. 
	public final static int XMID_Wakeup = 0x3E;
	public final static int XMID_WakeupAck = 0x3F;
	public final static int XMID_ReqDid = 0x00;
	public final static int XMID_DeviceId = 0x01;
	public final static int XMID_GotoConfig = 0x30;
	public final static int XMID_GotoConfigAck = 0x31;
	public final static int XMID_GotoMeasurement = 0x10;
	public final static int XMID_GotoMeasurementAck = 0x11;
	public final static int XMID_MtData2 = 0x36;
	public final static int XMID_ReqOutputConfig = 0xC0;
	public final static int XMID_SetOutputConfig = 0xC0;
	public final static int XMID_OutputConfig = 0xC1;
	public final static int XMID_Reset = 0x40;
	public final static int XMID_ResetAck = 0x41;
	public final static int XMID_Error = 0x42;
	public final static int XMID_ReqPeriodAck = 0x05;
	public final static int XMID_ReqPeriod = 0x04;
	public final static int XMID_ReqSTMessage = 0xD5;
	public final static int XMID_ReqSTMessageAck = 0xD4;
	public final static int XMID_SetNoRotation = 0x22;
	
	// XDI is a flag for the data a message contains. 
	public static int XDI_PacketCounter = 0x1020;
	public static int XDI_SampleTimeFine = 0x1060;
	public static int XDI_Quaternion = 0x2010;
	public static int XDI_DeltaV = 0x4010;
	public static int XDI_Acceleration = 0x4020;
	public static int XDI_RateOfTurn = 0x8020;
	public static int XDI_DeltaQ = 0x8030;
	public static int XDI_MagneticField = 0xC020;
	public static int XDI_StatusWord = 0xE020;
	public static int XDI_EulerAngles = 0x2030;
	public static int XDI_Temperature = 0x2040; // 2064 in decimal - temperature data
	public static int XDI_Pressure = 0x2050; // 2080 in decimal - pressure data
	public static int XDI_RGB = 0x2060; // 2096 in decimal- red, green, blue data
	public static int XDI_BAT = 0x2070; // 2112 in decimal- battery data
	
	
	/**
	 * Convert an signed byte to unsigned int vale. 
	 * @param b
	 * @return
	 */
	 public static int unsignedToBytes(byte b) {
		    return b & 0xFF;
	}
	 
	 
	 /*!
	  * \brief Calculate the number of bytes needed for \a message payload.
	  */
	 static int messageLength(XBusMessage message)
	 {
	 	switch (message.mid)
	 	{
	 	case XMID_SetOutputConfig:
	 		return message.len * 2 * 8;
	 	default:
	 		return message.len;
	 	}
	 }
	 
	 /*!
	  * \brief Format the payload of a message from a native data type to
	  * raw bytes.
	  */
	 static void formatPayload(int[] raw, XBusMessage message)
	 {
	 	int i;

	 	switch (message.mid)
	 	{
	 	//		case XMID_SetOutputConfig:
	 	//			formatOutputConfig(raw, message);
	 	//			break;
	 	default:
	 		for (i = 0; i < message.len; ++i)
	 		{
	 			raw[i] = message.charBufferRx[i];
	 		}
	 		break;
	 	}
	 }
	 
	
	/**
	 * \brief Format a message into the raw Xbus format ready for transmission to
	 * a motion tracker. Calculates the checksum.
	 * @param the buffer to fill. 
	 * @param the message to convert into raw bytes. 
	 * @return the length of the message.
	 */
	public static int XbusMessage_format(int[] raw, XBusMessage message){
		int count=0;  

		raw[count++] = XBUS_PREAMBLE;
		raw[count++] = XBUS_MASTERDEVICE;
			
		byte checksum = (byte) -XBUS_MASTERDEVICE;
		//System.out.println("checksum1 " + checksum);

		raw[count]= message.mid;
		//printf(" MID %u \n", message->mid);
		checksum -= raw[count++];
		//System.out.println("checksum2 " + checksum);

		int length = messageLength(message);
		//printf(" message length %d \n", length);


		if (length < XBUS_EXTENDED_LENGTH)
		{
			raw[count] = length;
			checksum -= raw[count++];
			//System.out.println("checksum3 " + checksum);

		}
		else
		{
			raw[count] = XBUS_EXTENDED_LENGTH;
			checksum -= raw[count++];
			
			raw[count] = length >> 8;
			checksum -= raw[count++];
			
			raw[count] = length & 0xFF;
			checksum -= raw[count++]; 
		}

		formatPayload(raw, message);
		int i;
		for (i = 0; i < length; ++i)
		{
			checksum -= raw[count++];
			//System.out.println("checksum4 " + checksum);
		}
		
		//now need to convert to unsigned byte- as java is signed store as int. 
		raw[count] =  unsignedToBytes(checksum);

		return count;
	}

	
	
	/**
	 * Do some testing! 
	 * @param args
	 */
    public static void main(String[] args) {		
    	//Note that goToMeasure should be  250, 255, 16, 0, 241
    	//and goToConfig is [250 255 48 0 209]
    	//and go to NoRotation  [250 255 192 0 65]
    	
    	int[] raw = new int[255]; 
    	
    	XBusMessage mtest = new XBusMessage(); 
    	//mtest.mid=XMID_GotoMeasurement;
    	mtest.mid=XMID_ReqOutputConfig;

    	
    	int len = XbusMessage_format(raw,  mtest);
    	
    	System.out.println("Raw data "+ len + " : ");
    	for (int i=0; i<len+1; i++) {
    		System.out.print(" " + raw[i]);
    	}
	}
	
	
	
	
}
	



