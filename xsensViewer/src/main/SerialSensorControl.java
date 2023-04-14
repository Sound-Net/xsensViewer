package main;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import comms.SerialComms;
import comms.SerialMessageParser;
import javafx.concurrent.Task;
import layout.SensorMessageListener;
import main.SensorsControl.SensorUpdate;
import xsens.XBusMessage;
import xsens.XSensMessage;
import xsens.XsMessageID;

/**
 * Main control class for communication with the sensor package. 
 * @author Jamie Macaulay
 *
 */
public class SerialSensorControl implements SensorControl {

	/**
	 * The serial parameters. 
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
	 * Listens for any updates form the sensor - e.g. whether they have been connected etc. 
	 */
	private ArrayList<SensorUpdateListener> updateListeners = new ArrayList<SensorUpdateListener>(); 

	/**
	 * The connected status flag
	 */
	private final static byte[] STATUS_CONNECTED=new byte[] {0x00};

	/**
	 * The unique ID of the sensor (if it has one)
	 */
	private Long ID = null;

	/**
	 * Raw buffer for message out.  
	 */
	private int[] messageOut = new int[255];

	/**
	 * Reference to the main controller. 
	 */
	private SensorsControl sensorsControl; 
	
	/**
	 * The device manager. 
	 */
	private DeviceManager deviceManager = new  DeviceManager(); 
	
	

	public SerialSensorControl(SensorsControl sensorsControl){
		this.sensorsControl=sensorsControl; 
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
		//System.out.println("serialTask: " + serialTask + "  " + (serialTask==null?null:serialTask.isCancelled())); 

		if (serialTask==null) return false; 
		if (serialTask.isCancelled()) return false;

		return true; 
	}

	/**
	 * Stop the serial port aquiring data and close the port. 
	 */
	public void stopSerial() {
		if (serialTask!=null) {
			this.connect=false;
			//very important to have interrupt as false or the serial thread does not cancel and keeps reading data, 
			serialTask.cancel(false);
		}
	}


	/**
	 * Sensor message listener. 
	 */
	public void addSensorMessageListener(SensorMessageListener sensorMessageListener){
		sensMessageListeners.add(sensorMessageListener); 
	}

	@Override
	public boolean removeSensorMessageListener(SensorMessageListener sensorMessageListener) {
		return sensMessageListeners.remove(sensorMessageListener); 
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
	 * Called whenever a new message is received from the device. 
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
	class SerialListener implements SerialPortDataListener {

		long count = 0; 

		/**
		 * The serial comm
		 */
		private SerialComms serialComm;


		SerialListener(SerialComms serialComm){
			this.serialComm=serialComm; 
		}


		@Override
		public void serialEvent(SerialPortEvent oEvent) {
			//System.out.println("Incomming data: " + oEvent.getEventType());
			if (oEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
				String inputLine=null;
				try {
					if (serialComm.getBufferredInput().ready()) {
						inputLine = serialComm.getBufferredInput().readLine();
						//System.out.println("Incomming data: " + inputLine);
						serialMessageParser.parseLine(inputLine); 
						count++;
					}

				} catch (Exception e) {
					System.err.println("Could not parse string: " + inputLine);
					System.err.println(e.toString());
					//e.printStackTrace();
				}
			}
		}
		
		public long getCount() {
			return count;
		}

		@Override
		public int getListeningEvents() {
			return  SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
		}

	}

	/* 
	 * Runs the serial thread. 
	 */
	int count=0;

	public class SerialTask extends Task<Integer> {

		private SerialListener serialListener;

		public SerialTask() {

		}

		@Override 
		protected Integer call() throws Exception {
			connect= true;
			
			try {


				serialComms.initialize();

				System.out.println("Init: " + serialComms.getSerialPort().isOpen());


				if (serialComms.getSerialPort()!=null){
					System.out.println("Add event listener;");
					serialComms.getSerialPort().removeDataListener();
					serialComms.getSerialPort().addDataListener(serialListener  = new SerialListener(serialComms));
					notifyUpdate(SensorUpdate.SENSOR_CONNECT, SerialSensorControl.this);
				}
				else {
					System.err.println("The Serial port is null");
					notifyUpdate(SensorUpdate.SENSOR_STOP, SerialSensorControl.this);
					return 0; 
				}


				while (connect==true && !isCancelled()){

					Thread.sleep(500);
					//				
					//				serialComms.getOutputStream().write(STATUS_CONNECTED);
					//				serialComms.getOutputStream().flush(); 
					if (serialComms.getSerialPort().isOpen()) {

						//write a byte to the output stream - this allows the 
						//device to recieve a few bytes of serial data and know it's connected to the PC. 
						if (count%6==0) {
							//System.out.println("Send output stream: " + isCancelled());
							serialComms.getOutputStream().write(STATUS_CONNECTED, 0, 1);
							serialComms.getOutputStream().flush(); 
						}
						
						if (serialListener.getCount()>10 && getSensorUID()==null) {
							System.out.println("REQUEST DEVICE ID");
							//request the device ID
							sendMessage(XsMessageID.XMID_GotoConfig);
							sendMessage(XsMessageID.XMID_ReqDid);
							Thread.sleep(100);
							sendMessage(XsMessageID.XMID_ReqDeviceType);
							sendMessage(XsMessageID.XMID_GotoMeasurement);
						}
						
						//System.out.println("Send output stream: 1" + isCancelled());
						if (count%30==0) {
							System.out.println("REQUEST SD SIZE");
							// request the SD card size. 
							sendMessage(XsMessageID.XMID_ReqSDSize);			
//							if (getSensorUID()==null) {
//								sendMessage(XsMessageID.XMID_GotoConfig);
//								sendMessage(XsMessageID.XMID_ReqDid);
//								Thread.sleep(100);
//								sendMessage(XsMessageID.XMID_GotoMeasurement);
//							}
						}
					}
					else {
						serialComms.initialize();
						serialComms.getSerialPort().openPort(1000);
						System.err.println("Serial port is not open: " + serialComms.getSerialPort().isOpen());
					}

					count++;

				}

			}
			catch (Exception e) {
				e.printStackTrace();
				serialComms.close();
				serialComms.getSerialPort().closePort();
				notifyUpdate(SensorUpdate.SENSOR_STOP, SerialSensorControl.this);
				return 0; 
			}

			serialComms.close();
			serialComms.getSerialPort().closePort();
			notifyUpdate(SensorUpdate.SENSOR_STOP, SerialSensorControl.this);

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

	/**
	 * Send a message to the device to go into deployment mode. 
	 */
	public void deploy() {
		//currently all that is needed is for the serial port to disconnect.
		//In future we can send messages to the device. 
		this.stopSerial();
	}

	/**
	 * Send a message to the sensor. This only supports sending a command with no associated data
	 * @param the command to send. 
	 */
	public void sendMessage(XsMessageID value) {
		XBusMessage mtest = new XBusMessage(); 
		//mtest.mid=XMID_GotoMeasurement;
		mtest.mid=value;
		sendMessage(mtest);  
	}


	/**
	 * Send a message to the sensor. This only supports sending a command with no associated data
	 * @param the command to send. 
	 */
	public void sendMessage(XsMessageID value, int[] data) {
		if (value == XsMessageID.XMID_SetRTCTime) {
			sendTimeMessage();
		}
		else {
			sendXMIDMessage(value, data); 
		}
	}


	private void sendXMIDMessage(XsMessageID value, int[] data) {
		XBusMessage mtest = new XBusMessage(); 
		//mtest.mid=XMID_GotoMeasurement;
		mtest.mid=value;
		mtest.charBufferRx=data;

		if (data!=null) {
			mtest.len = data.length;
		}
		else {
			mtest.len = 0; 
		}

		sendMessage(mtest);  
	}




	@Override
	public boolean stop() {
		this.stopSerial();
		return true;
	}

	@Override
	public void sendMessage(XBusMessage message) {
		int len = XSensMessage.XbusMessage_format(messageOut,  message);

		//now need to send the message
		try {
			//System.out.println(serialComms.getOutputStream());
			serialComms.getOutputStream().write(XSensMessage.raw2Bytes(messageOut), 0, len);

			serialComms.getOutputStream().flush();

//						System.out.println("----Raw bytes sent----");
//						for (int i=0; i<Math.min(XSensMessage.raw2Bytes(messageOut).length, 30); i++) {
//							System.out.print(XSensMessage.raw2Bytes(messageOut)[i] + " ");
//						}
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Send the current data time message
	 */
	public void sendTimeMessage() {

		//what is the current time
		//		long currentTime = System.currentTimeMillis(); 
		//		Date date = new Date(currentTime);		
		//		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
		//		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		
		//make sure this is UTC time!
		String timeS = 	LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"));

		System.out.println("Send time to device: " + timeS);
		//convert thre string to a byte array 
		byte[] byteArrray = timeS.getBytes();

		int[] data = new int[byteArrray.length]; 
		for (int i=0; i<byteArrray.length; i++) {
			data[i]=byteArrray[i];
			System.out.print(byteArrray[i] + " ");
		}


		sendXMIDMessage(XsMessageID.XMID_SetRTCTime, data); 

	}

	public static void main(String args[]) {

		int[] messageOut = new int[255]; 

		//what is the current time
		long currentTime = System.currentTimeMillis(); 

		System.out.println("Set current time millis: "); 

		Date date = new Date(currentTime);
		DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String timeS = formatter.format(date);

		System.out.println(timeS);
		//convert thre string to a byte array 
		byte[] byteArrray = timeS.getBytes();

		int[] data = new int[byteArrray.length]; 
		for (int i=0; i<byteArrray.length; i++) {
			data[i]=byteArrray[i];
			//System.out.print(byteArrray[i] + " ");
		}
		//System.out.println("");

		XBusMessage mtest = new XBusMessage(); 
		//mtest.mid=XMID_GotoMeasurement;
		mtest.mid=XsMessageID.XMID_SetRTCTime;
		mtest.charBufferRx=data;
		mtest.len = data.length;

		int len = XSensMessage.XbusMessage_format(messageOut,  mtest);

		//		System.out.println("----Raw bytes sent----" + mtest.len);
		//		for (int i=0; i<messageOut.length; i++) {
		//			System.out.print(messageOut[i] + " ");
		//		}

	}

	public boolean removeUpdateListener(SensorUpdateListener sensorUpdateListener) {
		return updateListeners.remove(sensorUpdateListener); 
	}

	public void addSensorUpdateListener(SensorUpdateListener sensorUpdateListener) {
		updateListeners.add(sensorUpdateListener); 
	}

	public void notifyUpdate(SensorUpdate sensorConnect, Object object) {
		for (int i=0; i<this.updateListeners.size(); i++) {
			updateListeners.get(i).notifyUpdate(sensorConnect, object); 
		}
	}

	@Override
	public String getSensorName() {
		if (serialComms.getSerialPort()==null) return null; 
		return serialComms.getSerialPort().getSystemPortPath();
	}

	@Override
	public boolean isConnected() {
		return this.isSerialRunning();
	}
	
	
	/**
	 * Get the device manager. The device manager provides information on what type 
	 * of device is connected. 
	 * @return the device manager
	 */
	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	@Override
	public Long getSensorUID() {
		return this.ID;
	}
	
	/**
	 * Set the unique ID of the sensor. 
	 * @param ID - the ID. 
	 */
	public void setSensorUID(Long ID) {
		 this.ID=ID;
	}

}