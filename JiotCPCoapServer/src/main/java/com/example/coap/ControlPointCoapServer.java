package com.example.coap;

import java.util.Collection;

import org.eclipse.californium.core.CoapServer;

import com.example.thing.ControlPoint;
import com.example.thing.ControlPointContainer;

public class ControlPointCoapServer extends CoapServer {

	@Override
	public synchronized void start() {
		ControlPointContainer cpContainer = ControlPointContainer.getInstance();
		cpContainer.start();
		
		Collection<ControlPoint> points = cpContainer.getControlPoints();
		for (ControlPoint point : points) {
			this.add(new ControlPointCoapResource(point));
		}
		
		super.start();
	}

	@Override
	public synchronized void stop() {
		ControlPointContainer.getInstance().stop();
		super.stop();
	}

}
