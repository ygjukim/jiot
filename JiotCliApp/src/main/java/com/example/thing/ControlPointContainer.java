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