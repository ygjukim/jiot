package com.example.thing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ControlPointContainer {

	private static AtomicReference<ControlPointContainer> instance = 
			new AtomicReference<ControlPointContainer>();
	
	public static ControlPointContainer getInstance() {
		if (instance.get() == null) {
			instance.set(new ControlPointContainer());
		}
		return instance.get();
	}
	
	private Map<Integer, ControlPoint> controlPoints = new HashMap<Integer, ControlPoint>();
	
	protected ControlPointContainer() {		
	}
	
	public void createControlPoints() {
		ControlPoint point = new GPIOPinOutputControlPoint(17);
		putControlPoint(point);
		
		point = new GPIOPinOutputControlPoint(27);
		putControlPoint(point);
		
		point = new GPIOPinOutputControlPoint(22);
		putControlPoint(point);
		
		point = new AsyncToggleOutputPoint(18);
		putControlPoint(point);
		
		point = new GPIOPinControlPoint(24);
		putControlPoint(point);
		
		for (int id=0; id<5; id++) {
			point = new PCF8591AnalogIOPoint(id);
			putControlPoint(point);
		}		
		point = new GPIOPinControlPoint(23);		// Alert
		putControlPoint(point);
	}

	public void start() {
		createControlPoints();

		for (ControlPoint cp : controlPoints.values()) {
			cp.open();
		}
	}
	
	public void stop() {
		for (ControlPoint cp : controlPoints.values()) {
			cp.close();
		}
		controlPoints.clear();
		ControlPoint.POLLING.shutdown();
	}
	
	public Collection<ControlPoint> getControlPoints() {
		return Collections.unmodifiableCollection(controlPoints.values());
	}
	
	public ControlPoint getControlPoint(int pointId) {
		return controlPoints.get(pointId);
	}
	
	public void putControlPoint(ControlPoint cp) {
		controlPoints.put(cp.getId(), cp);
	}
}