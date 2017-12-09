package com.example.coap;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class AsyncHelloCoapClient {
	private CoapClient client = null;
	
	public void run(String hostName) {
		client = new CoapClient("coap://" + hostName + ":5683" + "/hello?text=everybody");
		
		client.get(new CoapHandler() {
			@Override
			public void onError() {
				System.out.println("COAP Async Get: Cannot get nay response...");
			}

			@Override
			public void onLoad(CoapResponse response) {
				System.out.println("COAP Async Get: " + response.getResponseText());				
			}			
		});
		
		client.post(new CoapHandler() {

			@Override
			public void onError() {
				System.out.println("COAP Async Post: Cannot get nay response...");
			}

			@Override
			public void onLoad(CoapResponse response) {
				System.out.println("COAP Async Post: " + response.getResponseText());				
			}
			
		}, "everybody", MediaTypeRegistry.TEXT_PLAIN);
		
		System.out.println("Press enter to exit: ");		
		Scanner input = new Scanner(System.in);
		input.nextLine();
		System.out.println("Coap client stopped...");
		input.close();
	}
}
