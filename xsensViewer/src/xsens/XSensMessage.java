package xsens;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Set of messages which can be sent to the xsens sensor. 
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
//	 	case XMID_SetOutputConfig:
//	 		return message.len * 2 * 8;
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
	 * Get an XBUSMesage object from rw bytes 
	 * @param raw - the raw bytes
	 * @return the XBUSMesage containing the data. 
	 */
	public static XBusMessage XbusMessage_getXBusMessage(int[] raw){
		
		if (raw.length<5) {
			System.err.println("XbusMessage_getXBusMessage: xbus message length is less than 5"); 
			return null; 
		}
		
		if (raw[0]!=XBUS_PREAMBLE) {
			System.err.println("XbusMessage_getXBusMessage: xbus message length does not have a preamble"); 
			return null; 
		}
		
		
		XsMessageID messageID = XsMessageID.getXsMessageID(raw[2]); 
		
		if (messageID == null) {
			System.err.println("XbusMessage_getXBusMessage: xbus message does not have a valid XsMessageID"); 
			return null; 
		}
		
		
		XBusMessage xBusMessage = new XBusMessage();
		xBusMessage.mid = messageID; 
		
		if (raw[3] == 0xFF) {
			//extended message length 
			
			//TODO
			System.err.println("XbusMessage_getXBusMessage: Extended message length is not yet supported"); 

			return null; 
			
		}
		else {
			//standard message length
			//date length
			xBusMessage.len = raw[3]; 
			
			//the message data
			if (xBusMessage.len>0) {
				xBusMessage.charBufferRx = Arrays.copyOfRange(raw, 4, xBusMessage.len +4 ); 
			}
			else {
				xBusMessage.charBufferRx = null; 
			}
		}
		
		xBusMessage.checksum = raw[raw.length-1]; 
		
		
		return xBusMessage; 
		
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
	 * Format data for option flags. Options flags are used to set paramters in the 
	 * xsens device. 
	 * @param data - the data portion of an XSensMessage
	 * @param setFlags - the options flags to set (can be null).
	 * @param clearFlags - the options flags to clear (can be null). 
	 * @return the number of bytes of data (always 8). 
	 */
	public static int format_OptionConfig(int[] data, XsOptionID[] setFlags, XsOptionID[] clearFlags) {
	
		int setFlagInt = 0; 
		int clearFlagInt = 0; 
		
		if (setFlags != null) {
			for (int i=0;i<setFlags.length; i++) {
				setFlagInt = setFlagInt | setFlags[i].getValue();
			}
		}
		
		if (clearFlags !=null) {
			for (int i=0;i<clearFlags.length; i++) {
				clearFlagInt = clearFlagInt | clearFlags[i].getValue();
			}
		}
		
		
		ByteBuffer b = ByteBuffer.allocate(8);
		b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		b.putInt(setFlagInt);
		b.putInt(clearFlagInt);

		byte[] result = b.array();
		for (int i=0;i<result.length; i++) {
			//System.out.println(" result[i]: " +  result[i]);
			data[i] = result[i];
		}
		
		return result.length;
		
	}
	
	
	/**
	 * Get the options flags from xsens message. Returns a list of the flags that have been set in the 
	 * sensor. 
	 * @param data - input data bytes. 
	 * @return the options flags which have been set. 
	 */
	public static ArrayList<XsOptionID> get_OptionConfig(int[] data) {
				
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		
		for (int a = 0; a<data.length; a++) {
			b.put((byte) data[a]);
		}
		b.rewind(); //important to reset. 
		
		//Now we have our Integer.BYTES we get the individual flags
		XsOptionID[] values = XsOptionID.values(); 
		
		ArrayList<XsOptionID> setOptions = new 	ArrayList<XsOptionID>(); 
		
		int optionsMap =  b.getInt();
		
//		System.out.println("Binary Map: " + Integer.toString(optionsMap, 2) + " " + optionsMap);

		for (XsOptionID anOptionID : values) {

			if ((anOptionID.getValue() & optionsMap) != 0){
				setOptions.add(anOptionID);
			}
		}
				
		return setOptions; 
	}
	
	/**
	 * Set the output configuration for a SetOutputConfiguration message. The output configuration is, for example, 
	 * whether to return Euler angles or quaternions. 
	 * @param data - the data portion of an XSensMessage
	 * @param setFlags - the output configuration flags
	 * @param clearFlags - the corresponding desired output frequencies
	 * @return the number of bytes of output data
	 */
	public static int format_OutputConfiguration(int[] data, XsDataIdentifier[] outputTypes, int[] freq) {
		
		if (outputTypes != null) {
			//set up the byte buffer
			ByteBuffer b = ByteBuffer.allocate(4*outputTypes.length);
			b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

			for (int i=0;i<outputTypes.length; i++) {
				
				//the first 2 bytes are the output flag, the last two bytes are the frequency. 
				
				//bit shift the flag by eight to be in the first bytes. 
				int outputInt = outputTypes[i].getValue() << 16;  
				
				//now set the bits of the output frequency
				outputInt = outputInt | freq[i]; 
				
				b.putInt(outputInt); 
			}
			
			byte[] result = b.array();
			for (int i=0;i<result.length; i++) {
				data[i] = result[i];
				//System.out.println("output byte: " + result[i]);
			}
			
			return result.length; 
		}
		else {
			return 0;
		}
	}
	
	
	/**
	 * Get the options flags from xsens message. Returns a list of the flags that have been set in the 
	 * sensor. 
	 * @param data - input data bytes. 
	 * @return the options flags which have been set. 
	 */
	public static ArrayList<XsDataIdentifier> get_OutputConfig(int[] data) {
				
		ByteBuffer b = ByteBuffer.allocate(data.length);
		b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		
		for (int a = 0; a<data.length; a++) {
			b.put((byte) data[a]);
		}
		b.rewind(); //important to reset. 
		
		int nOuputs = data.length/4; 
		
		
		ArrayList<XsDataIdentifier> outputConfigs = new ArrayList<XsDataIdentifier>(); 
		
		XsDataIdentifier[] configValues =  XsDataIdentifier.values();
		int flag ;
		float freq;
		for (int i =0; i<nOuputs ; i++) {
			flag  = b.getShort();
			freq  = b.getShort();
			
			for (int j=0; j<configValues.length; j++) {
				if (configValues[j].getValue() == flag) {
					outputConfigs.add(configValues[j]);
					configValues[j].setFrequency(freq);
				}
			}
		}
				
		return outputConfigs; 
	}
	
	
	
	/**
	 * Do some testing! 
	 * @param args
	 */
    public static void main(String[] args) {		
    	//Note that goToMeasure should be  250, 255, 16, 0, 241
    	//and goToConfig is [250 255 48 0 209]
    	//and go to NoRotation  [250 255 192 0 65]
    	
    	System.out.println("Simple XMID_GotoMeasurement");

    	int[] raw = new int[255]; 
    	
    	XBusMessage mtest = new XBusMessage(); 
    	mtest.mid=XsMessageID.XMID_GotoMeasurement;
//    	mtest.mid=XsMessageID.XMID_ReqOutputConfig;
    	
    	
    	int len = XbusMessage_format(raw,  mtest);
    	
    	System.out.println("Raw data "+ len + " : ");
    	for (int i=0; i<len+1; i++) {
    		System.out.print(raw[i]+ " ");
    	}
		System.out.println(" ");
		
		


    	//lets send a output configuration - see page 23 of low level communication protocol. 
    	//Example → message for enabling AHS: FA FF 48 08 00 00 00 10 00 00 00 00 A1.
    	System.out.println("Set options flag");

    	XBusMessage mtOptionsTest = new XBusMessage(); 
    	
    	mtOptionsTest.mid=XsMessageID.XMID_SetOptionFlags;
    	mtOptionsTest.len = format_OptionConfig(mtOptionsTest.charBufferRx,new XsOptionID[] {XsOptionID.DisableAutoStore}, new XsOptionID[] {XsOptionID.DisableAutoMeasurement}); 
    	//mtOptionsTest.len = format_OptionConfig(mtOptionsTest.charBufferRx,new XsOptionID[] {XsOptionID.EnableAhs}, null); 

    	len = XbusMessage_format(raw,  mtOptionsTest);

       	System.out.println("Raw data "+ len + " : ");
    	for (int i=0; i<len+1; i++) {
    		System.out.print(String.format("%02X ",  (byte) raw[i]));
    	}
    	System.out.println("");

    	
    	
    	//This is what a message to set Euler angles should look like 
    	//36 <17:16:58.452> FA FF C0 10 10 20 FF FF 10 60 FF FF 20 30 00 64 E0 20 FF FF E3
    	
    	
    	System.out.println("Set output configuration");
    	
    	XBusMessage mtOutconfigTest = new XBusMessage(); 
    	
    	mtOutconfigTest.mid=XsMessageID.XMID_SetOutputConfig;
    	
    	mtOutconfigTest.len = format_OutputConfiguration(mtOutconfigTest.charBufferRx,new XsDataIdentifier[] {XsDataIdentifier.XDI_EulerAngles}, new int[] {100}); 

    	len = XbusMessage_format(raw,  mtOutconfigTest);

     	System.out.println("Raw data "+ len + " : ");
    	for (int i=0; i<len+1; i++) {
    		System.out.print(String.format("%02X ",  (byte) raw[i]));
    	}
    	
	}

	
}
	