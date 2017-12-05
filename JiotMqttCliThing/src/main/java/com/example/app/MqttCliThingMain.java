package com.example.app;

import java.util.logging.Level;

import org.jboss.logging.Logger;

import com.example.mqtt.MqttConsoleHandler;
import com.example.thing.ControlPoint;
import com.example.thing.ControlPointContainer;

public class MqttCliThingMain {

	public static void main(String[] args) {
		try {
			final ControlPointContainer container = ControlPointContainer.getInstance();
			container.start();
			
			final MqttConsoleHandler console = new MqttConsoleHandler();
			for (ControlPoint cp : container.getControlPoints()) {
				cp.addObserver(console);
			}
			
			Thread hookThread = new Thread() {
				public void run() {
					System.out.println("Program is shutdowning...");
					container.stop();
					console.close();
				}
 			 };
			
			 Runtime.getRuntime().addShutdownHook(hookThread);
			
			 for(;;)
				 Thread.sleep(1000);			 
		} catch (Exception ex) {
			Logger.getLogger(MqttCliThingMain.class.getName()).log(null, Level.SEVERE, null, ex);
		}
	}
}
