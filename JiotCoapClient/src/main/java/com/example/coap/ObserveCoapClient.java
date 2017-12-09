package com.example.coap;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class ObserveCoapClient {
	private CoapClient client = null;
	
	public void run(String hostName) {
		String uri = "coap://" + ((hostName != null)?hostName:"localhost") + ":5683" ;
		String resource = "/hello/count";
		client = new CoapClient(uri + resource);
		
		final CoapObserveRelation relation = client.observe(new CoapHandler() {
			@Override
			public void onError() {
				System.out.println("Error: Resource observation failed...");
			}

			@Override
			public void onLoad(CoapResponse response) {
				String count = response.getResponseText();
				System.out.println("Changed value of count : " + count);
			}			
		});

		Thread stopper = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Press enter to exit: ");		
				Scanner input = new Scanner(System.in);
				input.nextLine();
				relation.proactiveCancel();
				System.out.println("Coap client stopped...");
				input.close();
			}
			
		});
		
		stopper.start();
		try {
			stopper.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
