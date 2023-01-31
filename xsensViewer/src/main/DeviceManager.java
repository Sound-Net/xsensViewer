package main;

/**
 * Figures out what device is connected. 
 * @author Jamie Macaulay
 *
 */
public class DeviceManager {
	
	DeviceType currentDevice = null;  
	
	
	public enum DeviceType {
		SENSLOGGER_V1(200), SOUNDNET_V2_R1(10), SOUNDNET_V1_R5(5);
		
		int ID; 
	    private DeviceType(int ID) {
	    	this.ID = ID; 
	    }
	}

	/**
	 * Get the enum corresponding to a device type flag. 
	 * @param type - the type flag. 
	 * @return the corresponding devide enum. 
	 */
	public static DeviceType getDeviceType(int type) {
		DeviceType[] types = DeviceType.values();
		for (int i=0; i<types.length ; i++) {
			if (type  == types[i].ID) {
				return types[i]; 
			}
		}
		
		
		return null; 
	}
	
	/**
	 * Get the current connected device. 
	 * @return the current device enum. 
	 */
	public DeviceType getCurrentDevice() {
		return currentDevice;
	}

	/**
	 * Set the current connected device
	 * @param currentDevice - the current device enum to set.
	 */
	public void setCurrentDevice(DeviceType currentDevice) {
		this.currentDevice = currentDevice;
	}

	
	
//	/**
//	 * Get the device type from the ID. 
//	 * @param ID - the ID
//	 * @return - the ID. 
//	 */
//	public static DeviceType getDerviceType(long ID) {
//		
//		switch("") {
//		
//		}
//	
//	}

}
