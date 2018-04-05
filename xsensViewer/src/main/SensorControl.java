package main;

import java.util.ArrayList;

import comms.SerialComms;
import comms.SerialMessageParser;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.concurrent.Task;
import layout.SensorMessageListener;

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
	
	public void stopSerial() {
		serialTask.cancel(); 
	}
	
	
	/**
	 * Sensor message listener. 
	 */
	public void addSensorMessageListener(SensorMessageListener sensorMessageListener){
		sensMessageListeners.add(sensorMessageListener); 
	}

	
	
	/**
	 * The serial params 
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
						System.out.println("Incomming data: " + inputLine);
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
    public class SerialTask extends Task<Integer> {
  
		@Override protected Integer call() throws Exception {
        	serialComms.initialize();
        	
        	if ( serialComms.getSerialPort()!=null){
        		System.out.println("Add event listener;");
        		serialComms.getSerialPort().addEventListener(new SerialListener(serialComms));
        	
        	}
        	else return -1; 
        	
        	while (connect==true && !isCancelled()){
        		Thread.sleep(250);
        	}
        	
        	serialComms.close();
			return 0;
        }

    }

    
    /**
     * Get the message parser. This converts serial text to data values. 
     * @return the message parser
     */
	public SerialMessageParser getSerialParser() {
		return serialMessageParser; 
	}



}
