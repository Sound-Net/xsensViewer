package xsens;

/**
 * 
 * Class containing basic message information 
 * @author Jamie Macaulay 
 *
 */
public class XBusMessage {
	
	//default array size
	public static int ARRAY_SIZE = 255; 

	
	public int bid = XSensMessage.XBUS_MASTERDEVICE; 
	

	/*! \brief The message ID of the message. */
	public XsMessageID mid;

	/*!
	 * \brief The length of the payload.
	 *
	 * \note The meaning of the length is message dependent. For example,
	 * for XMID_OutputConfig messages it is the number of OutputConfiguration
	 * elements in the configuration array.
	 */
	public int len = 0; 

	/*!
	 * \brief contains all data within a message
	 */
	public int[] charBufferRx = new int[ARRAY_SIZE]; 

	/**
	 * Checksum
	 */
	public int checksum;

}
