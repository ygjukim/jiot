package com.example.mqtt;

import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.json.Json;
import javax.json.JsonObject;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.eclipse.paho.client.mqttv3.MqttClient;

public class JmsCliConsole {
    public static final String TOPIC_PREFIX = "jiot.mqtt.thing.";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "%s.%s.command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s.result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s.broadcast";

    private String url;
    private String clientId;
    private MqttClient client;
    private Connection connection;
    private Session session;

    private String commandTopicName;
    private String resultTopicName;
    private String broadcastTopicName;
    private MessageProducer commandProducer;
    private MessageConsumer resultConsumer;
    private MessageConsumer broadcastConsumer;
    private BigDataHandler bdHandler;
    
    public JmsCliConsole(String clientId, String handlerId, BigDataHandler bdHandler) throws JMSException {
    	this.clientId = clientId;
    	this.bdHandler = bdHandler;
    	
    	url = System.getProperty("jms.server", "tcp://127.0.0.1:61616");
    	commandTopicName = String.format(TOPIC_COMMAND, clientId, handlerId);
    	resultTopicName = String.format(TOPIC_RESULT, clientId);
    	broadcastTopicName = String.format(TOPIC_BROADCAST, handlerId);
    	
    	System.out.println("JMS server url: " + url);
    	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);    	
    	connection = connectionFactory.createConnection();
    	connection.start();
    	
    	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    	Topic commandTopic = session.createTopic(commandTopicName);
    	Topic resultTopic = session.createTopic(resultTopicName);
    	Topic broadcastTopic = session.createTopic(broadcastTopicName);
    	
    	commandProducer = session.createProducer(commandTopic);
    	
    	resultConsumer = session.createConsumer(resultTopic);
    	resultConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				try {
					System.out.println("Result: " + convertMessageToString(msg));
					displayPrompt();
				} catch (JMSException e) {
					Logger.getLogger(JmsCliConsole.class.getName()).log(Level.SEVERE, null, e);
				} catch (IOException e) {
					Logger.getLogger(JmsCliConsole.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});
    	
    	broadcastConsumer = session.createConsumer(broadcastTopic);
    	broadcastConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				try {
					StringReader in = new StringReader(convertMessageToString(msg));
					JsonObject jsonObj = Json.createReader(in).readObject();
					String broadcastType = jsonObj.getString("type");
					if (broadcastType.equals(ChangeOfValue.TYPE)) {
						saveData(new ChangeOfValue(jsonObj));
					}
					else {
						System.out.println("Received a broadcast: " + msg);
					}
				} catch (JMSException e) {
					Logger.getLogger(JmsCliConsole.class.getName()).log(Level.SEVERE, null, e);
				} catch (IOException e) {
					Logger.getLogger(JmsCliConsole.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});
    }

    public void publish(String payload) throws JMSException {
    	BytesMessage message = session.createBytesMessage();
    	message.writeBytes(payload.getBytes());
    	commandProducer.send(message);
    }
    
    public void saveData(ChangeOfValue cov) {
    	bdHandler.saveBigData(cov);
    }
    
    public void close() throws JMSException {
    	connection.stop();
    	commandProducer.close();
    	resultConsumer.close();
    	broadcastConsumer.close();
    	session.close();
    	connection.close();    	
    }
    
    private String convertMessageToString(Message msg) throws JMSException, IOException {
    	if (!(msg instanceof BytesMessage)) {
    		throw new JMSException("Only BytesMessage is processible");
    	}
    	
    	BytesMessage response = (BytesMessage)msg;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	byte[] buffer = new byte[512];
    	int count = 0;
    	while ((count = response.readBytes(buffer)) != -1) {
    		baos.write(buffer, 0, count);
    	}
    	
    	return new String(baos.toByteArray());
    }

    public static void displayPrompt() {
    	System.out.print("Input command or 'q'(quit) > ");
    }
    
    public static void main(String[] args) {
    	Scanner input = new Scanner(System.in);
    	System.out.print("Input client ID: ");
    	String clientId = input.nextLine();
    	System.out.print("Input the IP address of thing: ");
    	String handlerId = input.nextLine().replace('.', '_');

    	try {
        	System.out.print("JmsCliConsole connecting...");
        	BigDataHandler bdHandler = new BigDataHandler();
			JmsCliConsole console = new JmsCliConsole(clientId, handlerId, bdHandler);
			
			displayPrompt();
			for (String line = input.nextLine(); !line.equals("q"); line = input.nextLine()) {
				if (line.trim().length() == 0) {
					continue;
				}
				
				if (line.equals("display")) {
					bdHandler.displayBigData();
					displayPrompt();
				}
				else if (line.equals("clear")) {
					bdHandler.clearBigData();
					displayPrompt();
				}
				else {
					console.publish(line);
					System.out.println("Waitting the result from thing...");
				}
			}
			
			console.close();
		} catch (JMSException e) {
			Logger.getLogger(JmsCliConsole.class.getName()).log(Level.SEVERE, null, e);
		}
    	
    	input.close();
    }
}
