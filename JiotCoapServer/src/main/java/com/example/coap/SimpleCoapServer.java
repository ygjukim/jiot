package com.example.coap;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class SimpleCoapServer {
	private CoapServer server = null;
	
	public void start() {
		server = new CoapServer();
		
		CoapResource resource = new CoapResource("hello") {
			@Override
			public void handleGET(CoapExchange exchange) {
				String text = exchange.getRequestOptions().getUriQuery().get(0);
				System.out.println(getName() +  "'called GET method");
				exchange.respond("Hello " + text.substring(text.indexOf("=") + 1));
			}

			@Override
			public void handlePOST(CoapExchange exchange) {
				String text = exchange.getRequestText();
				System.out.println(getName() +  "'called POST method");
				exchange.respond("Hello " + text);
			}			
		};
		
		resource.add(new CoapResource("world") {
			@Override
			public void handleGET(CoapExchange exchange) {
				String text = exchange.getRequestOptions().getUriQuery().get(0);
				System.out.println(getName() +  "'called GET method");
				exchange.respond("Hello World " + text.substring(text.indexOf("=") + 1));
			}

			@Override
			public void handlePOST(CoapExchange exchange) {
				String text = exchange.getRequestText();
				System.out.println(getName() +  "'called POST method");
				exchange.respond("Hello World " + text);
			}			
		});
		
		resource.add(new CoapResource("count") {
			private int count;
			
			private CoapResource initialize() {
				setObservable(true);
				setObserveType(Type.CON);
				getAttributes().setObservable();
				
				start();
				
				return this;
			}
			
			private void start() {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						increment();
						changed();						
					}}, 1000, 1000);
			}
			
			private void increment() {
				count++;
			}

			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println(getName() +  "'called Get method");
				exchange.respond(String.valueOf(count));
			}
			
		}.initialize());
		
		server.add(resource);
		server.start();
	}
	
	public static void main(String[] args) {
		System.out.println("Start Simple COAP Server...");		
		SimpleCoapServer coapServer = new SimpleCoapServer();
		coapServer.start();
	}
}
