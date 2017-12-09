package com.example.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.mqtt.MqttConsoleHandler;
import com.example.thing.ControlPoint;
import com.example.thing.ControlPointContainer;

public class MqttCliThingMain {
	private static ControlPointContainer container = null;
	private static MqttConsoleHandler console = null;
	
	public static void close() {
		if (container != null)  container.stop();
		if (console != null)  console.close();
	}
	
	public static void main(String[] args) {
		try {
			container = ControlPointContainer.getInstance();
			container.start();
			
			console = new MqttConsoleHandler();
			for (ControlPoint cp : container.getControlPoints()) {
				cp.addObserver(console);
			}
			
			Thread hookThread = new Thread() {
				public void run() {
					System.out.println("Program is shutdowning...");
					close();
				}
 			 };
			
			 Runtime.getRuntime().addShutdownHook(hookThread);
			
			 for(;;)
				 Thread.sleep(1000);			 
		} catch (Exception ex) {
			Logger.getLogger(MqttCliThingMain.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
