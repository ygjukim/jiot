package com.example.mqtt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.example.cli.Commander;
import com.example.thing.ControlPoint;

public class MqttConsoleHandler implements Observer {
	public static final String TOPIC_PREFIX = "jiot/mqtt/thing/";
	public static final String TOPIC_COMMAND = TOPIC_PREFIX + "+/%s/command";
	public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s/result";
	public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s/broadcast";
	
	private ExecutorService commandThread = Executors.newCachedThreadPool();
	private String uri;
	private String clientId;
	private MqttClient client;
	private String commandTopic;
	private String broadcastTopic;
		
	public MqttConsoleHandler() throws Exception {
		uri = System.getProperty("mqtt.server", "tcp://127.0.0.1:1883");
		System.out.println("MQTT server uri : " + uri);
		
		String ipAddress = getLocalIPAddress();
		if (ipAddress == null) {
			throw new Exception("Cannot find IP address of this thing");
		}
		System.out.println("Used IP address: " + ipAddress);
		clientId = ipAddress.replace('.', '_');
		
		commandTopic = String.format(TOPIC_COMMAND, clientId);
		broadcastTopic = String.format(TOPIC_BROADCAST, clientId);
		System.out.println("Command Topic : " + commandTopic);
		System.out.println("Broadcast Topic : " + broadcastTopic);
		
		String tmpDir = System.getProperty("java.io.tmpdir");
		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
		System.out.println("Default file persistence: " + tmpDir);
		
		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		
		client = new MqttClient(uri, clientId, dataStore);
		
		client.setCallback(new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				Logger.getLogger(MqttConsoleHandler.class.getName()).log(Level.SEVERE, null, cause);
				System.exit(-1);
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				try {
					Logger.getLogger(MqttConsoleHandler.class.getName()).
						log(Level.INFO, String.format("Delivered - [%s] message: %s ", new Date(), token.getMessage()));
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				final String[] clientInfo = topic.substring(TOPIC_PREFIX.length()).split("/");
				final String commandStr = message.toString();
				
				System.out.println("Message received from " + topic + " : " + message);
				
				commandThread.submit(new Runnable() {
					@Override
					public void run() {
						String[] command = commandStr.split(" ");
						String result;
						
						try {
							result = Commander.getInstance().execute(command);
							result = (result == null) ? "Success" : result;
						} catch (IOException e) {
							//e.printStackTrace();
							result = "Exception happened in executing '" + commandStr + "' : " + e.getMessage();
						}
						
						try {
							publish(clientInfo[0], result, 1);
						} catch (MqttPersistenceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
		
		client.connect(connOpt);
		client.subscribe(commandTopic);
	}
	
	public void publish(String clientId, String payload, int qos) throws MqttPersistenceException, MqttException {
		MqttTopic topic = client.getTopic(String.format(TOPIC_RESULT, clientId));
		topic.publish(payload.getBytes(), qos, false);
	}
	
	public void broadcast(String payload) throws MqttPersistenceException, MqttException {
		MqttTopic topic = client.getTopic(broadcastTopic);
		topic.publish(payload.getBytes(), 0, false);
	}
	
	public void close() {
		if (client != null) {
			try {
				client.disconnect();
				client.close();
				client = null;
			} catch (MqttException e) {
				Logger.getLogger(MqttConsoleHandler.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}
	
	@Override
	public void update(Observable obv, Object arg) {
		if (obv instanceof ControlPoint) {
			ControlPoint cp = (ControlPoint)obv;
			JsonObject jsonObj = Json.createObjectBuilder()
										.add("type", "cov")
										.add("handlerId", clientId)
										.add("pointId", cp.getId())
										.add("pointName", cp.getName())
										.add("value", cp.getPresentValue())
										.build();
			try {
				broadcast(jsonObj.toString());
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String getLocalIPAddress() throws SocketException {
		String ipAddress = null;

		Enumeration<NetworkInterface> networkList = NetworkInterface.getNetworkInterfaces();
		NetworkInterface ni;
		InetAddress inetAddr = null;
		
		while (networkList.hasMoreElements()) {
			ni = networkList.nextElement();
			Enumeration<InetAddress> addresses = ni.getInetAddresses();
			while (addresses.hasMoreElements()) {
				inetAddr = (InetAddress) addresses.nextElement();
              if (!inetAddr.isLoopbackAddress()) {
                  if (inetAddr.isSiteLocalAddress()) {
                      // Found non-loopback site-local address. Return it immediately...
                      return inetAddr.getHostAddress();
                  	}
              	}
			}
		}
		
		try {
			inetAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inetAddr.getHostAddress();		
	}
}
