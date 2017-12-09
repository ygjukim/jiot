package com.example.coap;

import java.util.Scanner;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.ResourceAttributes;

public class DiscoveryCoapClient {
	private CoapClient client = null;
	
	public void run(String hostName) {
		String uri = "coap://" + ((hostName != null)?hostName:"localhost") + ":5683" ;
		String resource = "/.well-known/core";
		client = new CoapClient(uri + resource);
		
		client.get(new CoapHandler() {
			@Override
			public void onError() {
				System.out.println("Error: Cannot discovery COAP devices and resources...");
			}

			@Override
			public void onLoad(CoapResponse response) {
				System.out.println("Async. Get: " + response.getResponseText());
				
				if (response.getOptions().getContentFormat() == 
						MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
					Set<WebLink> links = LinkFormat.parse(response.getResponseText());
					for (WebLink link : links) {
						System.out.println(link.getURI());
						ResourceAttributes attrs = link.getAttributes();
						Set<String> keys = attrs.getAttributeKeySet();
						for (String key : keys) {
							System.out.println(key + ": " + attrs.getAttributeValues(key));
						}
						System.out.println("==============================================");
					}
				}
			}			
		});

		System.out.println("Press enter to exit: ");		
		Scanner input = new Scanner(System.in);
		input.nextLine();

		System.out.println("Coap client stopped...");
		input.close();		
	}
}
