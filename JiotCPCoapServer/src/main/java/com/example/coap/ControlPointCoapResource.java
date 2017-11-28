package com.example.coap;

import java.io.StringReader;
import java.util.Observable;
import java.util.Observer;

import javax.json.Json;
import javax.json.JsonObject;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.example.thing.ControlPoint;
import com.example.thing.OutputControlPoint;

public class ControlPointCoapResource extends CoapResource {
	private ControlPoint point = null;
	
	public ControlPointCoapResource(ControlPoint point) {
		super(String.valueOf(point.getId()));
		this.point = point;
		addChildResources();
	}
	
	private void addChildResources() {
		add(new CoapResource("properties") {
			private CoapResource initialize() {
				setObservable(true);
				setObserveType(CoAP.Type.CON);
				getAttributes().setObservable();
				point.addObserver(new Observer() {
					@Override
					public void update(Observable o, Object arg) {
						if (arg != null) {
							changed();
						}
					}
				});
				return this;
			}

			@Override
			public void handleGET(CoapExchange exchange) {
				JsonObject jsonObj = Json.createObjectBuilder()
											.add("Id", point.getId())
											.add("Type", point.getType().name())
											.add("Name", point.getName())
											.add("Enabled", point.isEnabled())
											.build();
				exchange.respond(jsonObj.toString());
			}

			@Override
			public void handlePOST(CoapExchange exchange) {
				String jsonStr = exchange.getRequestText();
				if (jsonStr != null) {
					System.out.println("[DEBUG] " + jsonStr);
					try {
						JsonObject jsonObj = Json.createReader(new StringReader(jsonStr)).readObject();
						point.setName(jsonObj.getString("Name"));
						exchange.respond("true");
					} catch (Throwable ex) {
						exchange.respond("Exception occured : " + ex.getMessage());
					}
				}
				else {
					exchange.respond("false");
				}
			}
		}.initialize());
		
		add(new CoapResource("presentValue") {
			private CoapResource initialize() {
				setObservable(true);
				setObserveType(CoAP.Type.CON);
				getAttributes().setObservable();
				point.addObserver(new Observer() {
					@Override
					public void update(Observable o, Object arg) {
						if (arg == null) {
							changed();
						}
					}
				});
				return this;
			}

			@Override
			public void handleGET(CoapExchange exchange) {
				exchange.respond(String.valueOf(point.getPresentValue()));;
			}

			@Override
			public void handlePOST(CoapExchange exchange) {
				if (point instanceof OutputControlPoint) {
					System.out.println("[DEBUG] " + exchange.getRequestText());																																																																																																																																																							
					try {
						int presentValue = Integer.parseInt(exchange.getRequestText());
						((OutputControlPoint)point).setPresentValue(presentValue);
						exchange.respond("true");
					} catch (Throwable ex) {
						exchange.respond("Exception occured : " + ex.getMessage());
					}
				}
				else {
					exchange.respond("false");
				}
			}
		}.initialize());
	}

}
