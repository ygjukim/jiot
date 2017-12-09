package com.example.coap;

public class CoapClientMain {
	
	public static void main(String[] args) {
//		HelloCoapClient client = new HelloCoapClient();
//		AsyncHelloCoapClient client = new AsyncHelloCoapClient();
//		ObserveCoapClient client = new ObserveCoapClient();
		DiscoveryCoapClient client = new DiscoveryCoapClient();
		
		client.run(args[0]);				
	}

}
