package com.example.coap;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class HelloCoapClient {
	private CoapClient client = null;
	
	public void run(String hostName) {
		client = new CoapClient();
		CoapResponse response;
		String uri;
		
		Scanner input = new Scanner(System.in);
		System.out.println("Enter Path(or 'q' to stop): ");
		for (String line = input.nextLine();
				!line.equals("q"); line = input.nextLine()) {
			uri = "coap://" + hostName + ":5683" + line + "?text=everybody";
			System.out.println("URI: " + uri);
			client.setURI(uri);
			
			response = client.get();
			
			if (response != null) {
				System.out.println("code: " + response.getCode());
				System.out.println("options: " + response.getOptions());
				System.out.println("payload: " + Utils.toHexString(response.getPayload()));
				System.out.println("text: " + response.getResponseText());
				System.out.println("advanced: " + Utils.prettyPrint(response));				
			}
			
			uri = "coap://" + hostName + ":5683" + line;
			client.setURI(uri);
			
			System.out.println("Post: " + client.post("everybody",  MediaTypeRegistry.TEXT_PLAIN).getResponseText());

			System.out.println("Enter Path(or 'q' to stop): ");			
		}
		
		input.close();
		System.out.println("Coap client stopped...");
	}
}
