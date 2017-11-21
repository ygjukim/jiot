package com.example.thing;

public class TestControlPointContainer extends ControlPointContainer {

	@Override
	public void createControlPoints() {
		ControlPoint point = new GPIOPinOutputControlPoint(17);
		putControlPoint(point);
		
		point = new GPIOPinOutputControlPoint(27);
		putControlPoint(point);
		
		point = new GPIOPinOutputControlPoint(22);
		putControlPoint(point);
		
		point = new AsyncToggleOutputPoint(18);
		putControlPoint(point);
		
		for (int id=0; id<5; id++) {
			point = new PCF8591AnalogIOPoint(id);
			putControlPoint(point);
		}		
	}

}
