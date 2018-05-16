package xsens;

//structure to hold message data
public class XBusData
 {
	
   /*! \brief the type of data. */
   public int dataid;


   /*!
  * \brief The length of the payload.
   *
   * \note The meaning of the length is message dependent. For example,
   * for XMID_OutputConfig messages it is the number of OutputConfiguration
   * elements in the configuration array.
   */
   public int len;

   /*!
    * \brief contains all data within a message
   */
   public int[] data= new int[XBusMessage.ARRAY_SIZE];
   
   
 }