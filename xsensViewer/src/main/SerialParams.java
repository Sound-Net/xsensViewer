package main;


public class SerialParams implements Cloneable {
	
	/**
	 * The baud rate
	 */
	public int baudRate=38400;
	
	/**
	 * The com port
	 */
	public String port = "COM3"; 
	
	
	/* (non-Javadoc)
	 * @see fileOfflineData.OfflineFileParams#clone()
	 */
	@Override
	protected SerialParams clone() {
		try {
			return (SerialParams) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}



}
