package com.example.mqtt;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;

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
import org.jboss.logging.Logger;

public class MqttEchoReceiver {
	public  static final String TOPIC_PREFIX = "jiot/mqtt";
	public static final String TOPIC_ECHO = TOPIC_PREFIX + "+/echo";
	public static final String TOPIC_RESPONSE = TOPIC_PREFIX + "%s/response";
	
	private String uri;
	private String clientId;
	private MqttClient client;
	
	public MqttEchoReceiver() throws SocketException, MqttException {
		uri = System.getProperty("mqtt.server", "tcp://localhost:1883");
		String ipAddress = getLocalIPAddress();		
		System.out.println("Used IP address: " + ipAddress);
//		clientId = ipAddress.replace('.', '_');
		clientId = "mqttechoreceiver";
		String tmpDir = System.getProperty("java.io.tmpdir");
		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
		
		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		
		client = new MqttClient(uri, clientId, dataStore);
		client.setCallback(new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				Logger.getLogger(MqttEchoReceiver.class.getName()).log(null, Level.SEVERE, null, cause);
				System.exit(-1);
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				try {
					Logger.getLogger(MqttEchoReceiver.class.getName()).info(String.format("Delivered - [%s] message: %s", new Date(), token.getMessage().toString()));
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void messageArrived(String topic, MqttMessage msg) throws Exception {
				String[] clientInfo = topic.substring(TOPIC_PREFIX.length()).split("/");
				JsonObject jsonObj = Json.createObjectBuilder()
										.add("clientId", clientInfo[0])
										.add("message", msg.toString())
										.build();
				System.out.println("Publish a message: " + jsonObj.toString());
				publish(clientInfo[0], jsonObj.toString(), 1);
			}
		});
		
		client.connect(connOpt);
//		client.subscribe(TOPIC_ECHO);
	}
	
	public void publish(String clientId, String payload, int qos) throws MqttPersistenceException, MqttException {
		MqttTopic topic = client.getTopic(String.format(TOPIC_RESPONSE, clientId));
		topic.publish(payload.getBytes(), qos, false);
	}
	
	public void subscribe() throws MqttException {
		client.subscribe(TOPIC_ECHO);
	}
	
	public String getLocalIPAddress() throws SocketException {
		String ipAddress = null;
		Enumeration<NetworkInterface> networkList = NetworkInterface.getNetworkInterfaces();
		NetworkInterface ni;
		
		while (networkList.hasMoreElements()) {
			ni = networkList.nextElement();
			if (!ni.getName().equals("enp3s0")) {
				continue;
			}
			
			Enumeration<InetAddress> addresses = ni.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ipAddress = addresses.nextElement().getHostAddress();
				break;
			}
			
			if (ipAddress != null)  break;
		}
		
		return ipAddress;
	}
	
	public void close() {
		if (client != null) {
			try {
				client.disconnect();
				client.close();
				client = null;
			} catch (MqttException e) {
				e.printStackTrace();
//				Logger.getLogger(MqttEchoReceiver.class.getName()).log(null, Level.SEVERE, null, e);
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			final MqttEchoReceiver receiver = new MqttEchoReceiver();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					receiver.close();
				}
			});
			
			receiver.subscribe();
		} catch (Exception ex) {
			ex.printStackTrace();
//			Logger.getLogger(MqttEchoReceiver.class.getName()).log(null, Level.SEVERE, null, ex);
		} 
	}
}
