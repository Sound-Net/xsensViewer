package main;

import java.util.ArrayList;

import comms.SerialComms;
import comms.SerialMessageParser;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.concurrent.Task;
import layout.SensorMessageListener;

/**
 * Main control class for communication with the sensor package. 
 * @author Jamie Macaulay
 *
 */
public class SensorControl extends SensorMain {
	
	/**
	 * The serial paramters
	 */
	private SerialParams params; 
	
	/**
	 * The serial communication task/ 
	 */
	private SerialComms serialComms; 
	
	/**
	 * True if connected to the serial port. 
	 */
    private volatile boolean connect = true; 

    /**
     * Task running the serial thread. 
     */
	private SerialTask serialTask;
	
	/**
	 * Serial message parser
	 */
	private SerialMessageParser serialMessageParser; 
	
	/**
	 * Array of listeners for sensor messages
	 */
	ArrayList<SensorMessageListener> sensMessageListeners; 
	
	/**
	 * The connected status flag
	 */
	private final static byte[] STATUS_CONNECTED=new byte[] {0x00};

	
	public SensorControl(){
		params= new SerialParams();
		serialComms= new SerialComms(); 
		serialMessageParser= new SerialMessageParser(this);
		sensMessageListeners= new ArrayList<SensorMessageListener>(); 
		
	}

	/**
	 * Start streaming serial data. 
	 */
	public void startSerial() {
		serialComms.setPort(params.port); 
		serialComms.setBaud(params.baudRate);
		
		connect=true; 
		serialTask = new SerialTask();
		Thread th = new Thread(serialTask);
		th.setDaemon(true);
		th.start();
	}
	
	/**
	 * Check whether the serial port is sending data to the program
	 * @return true if the serial port is open and sending data. 
	 */
	public boolean isSerialRunning() {
		if (serialTask==null) return false; 
		if (serialTask.isCancelled()) return false;
		return true; 
	}
	
	/**
	 * Stop the serial port aquiring data and close the port. 
	 */
	public void stopSerial() {
		serialTask.cancel(false);
	}
	
	
	/**
	 * Sensor message listener. 
	 */
	public void addSensorMessageListener(SensorMessageListener sensorMessageListener){
		sensMessageListeners.add(sensorMessageListener); 
	}

	
	
	/**
	 * Get the serial params. Baudrate etc. 
	 * @return the params
	 */
	public SerialParams getParams() {
		return params;
	}

	/**
	 * 
	 * Set the Serial params. 
	 * @param params the params to set
	 */
	public void setParams(SerialParams params) {
		this.params = params;
	}

    /**
     * Called whenever a new message is recieved from the device. 
     * @param sensorComms - the sensor message
     */
	public void newMessage(SensorData sensorComms) {
		for (int i =0; i<sensMessageListeners.size(); i++){
			sensMessageListeners.get(i).newSensorMessage(sensorComms);
		}
	}
	
	/**
	 * Listens for data on the specified serial port untill told to stop. 
	 * @author Jamie Macaulay
	 *
	 */
	class SerialListener implements SerialPortEventListener {
		
		/**
		 * The serial comm
		 */
		private SerialComms serialComm;


		SerialListener(SerialComms serialComm){
			this.serialComm=serialComm; 
		}
		

		@Override
		public void serialEvent(SerialPortEvent oEvent) {
			if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					String inputLine=null;
					if (serialComm.getBufferredInput().ready()) {
						inputLine = serialComm.getBufferredInput().readLine();
						//System.out.println("Incomming data: " + inputLine);
						serialMessageParser.parseLine(inputLine); 
					}

				} catch (Exception e) {
					System.err.println(e.toString());
				}
			}
			
		}
		
	}

	/* 
	 * Runs the serial thread. 
	 */
	int count=0;
    public class SerialTask extends Task<Integer> {
  
		@Override protected Integer call() throws Exception {
			try {
        	serialComms.initialize();
        	
        	if ( serialComms.getSerialPort()!=null){
        		//System.out.println("Add event listener;");
        		serialComms.getSerialPort().addEventListener(new SerialListener(serialComms));
        	}
        	else return -1; 
        	
        	while (connect==true && !isCancelled()){
        		Thread.sleep(500);
        		//write a byte to the outpur stream - this allows the 
        		//device to recieve a few bytes of serial data and know it's connecyted to the PC. 
        		count++;
        		if (count%6==0) {
            		System.out.println("Send output stream: " + isCancelled());
            		serialComms.getOutputStream().write(STATUS_CONNECTED);
        		}
        	}
        	
        	serialComms.close();
        	serialComms.getSerialPort().close();
			return 0;
			}
			catch (Exception e) {
				e.printStackTrace();
				return -1; 
			}
        }

    }
 

    
    /**
     * Get the message parser. This converts serial text to data values. 
     * @return the message parser
     */
	public SerialMessageParser getSerialParser() {
		return serialMessageParser; 
	}

	/**
	 * Send a message to the device to go into deployment mode. 
	 */
	public void deploy() {
		//currently all that is needed is for the serial port to disconnect.
		//In future we can send messages to the device. 
		this.stopSerial();
	}



}