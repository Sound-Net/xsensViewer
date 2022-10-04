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

	
	/**
	 * Converts an array of ints with unsigned byte values to java signed bytes
	 * to be sent throught the serial port
	 * @param mtest
	 * @return
	 */
	public static byte[] raw2Bytes(int[] raw) {
		byte[] array = new byte[raw.length];
		for (int i=0; i<raw.length; i++){
			array[i]=signedByte(raw[i]);
		}
		return array;
	}
	
	/**
	 * Convert an signed byte to unsigned int vale. 
	 * @param b input byte
	 * @return unisgned in value
	 */
	 public static int unsignedToBytes(byte b) {
		    return b & 0xFF;
	}
	 
	 /**
	  * Get the signed byte value from unisgned byte within int (because java default is signed)
	  * @return the signed byte value. 
	  */
	 public static byte signedByte(int bytevalue) {
		 return (byte) ((bytevalue << 24) >> 24); 
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
	 static void formatPayload(int[] raw, XBusMessage message, int count)
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
	 			raw[count+i] = message.charBufferRx[i];
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

		raw[count]= message.mid.getValue();
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


		formatPayload(raw, message, count);
		
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
    	mtest.mid=XsMessageID.XMID_ReqOutputConfig;

    	
    	int len = XbusMessage_format(raw,  mtest);
    	
    	System.out.println("Raw data "+ len + " : ");
    	for (int i=0; i<len+1; i++) {
    		System.out.print(" " + raw[i]);
    	}
	}
	
}
	