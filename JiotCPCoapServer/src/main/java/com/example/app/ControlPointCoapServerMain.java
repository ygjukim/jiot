package com.example.app;

import com.example.coap.ControlPointCoapServer;

public class ControlPointCoapServerMain {

	private static Thread mainThread;
	
	public static void main(String[] args) {
		mainThread = Thread.currentThread();
		
		final ControlPointCoapServer server = new ControlPointCoapServer();
		server.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("CoAP server is now shutdowned...");
				server.stop();
				mainThread.interrupt();
			}
		});
	}

}
