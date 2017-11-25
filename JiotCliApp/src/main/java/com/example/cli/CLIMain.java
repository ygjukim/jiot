package com.example.cli;

import java.io.IOException;

import com.example.thing.ControlPoint;
import com.example.thing.ControlPointContainer;

import jdk.dio.uart.UARTConfig;

public class CLIMain {
    public static void main(String[] args) throws IOException {
    	 ControlPointContainer pointHandler = ControlPointContainer.getInstance();
        pointHandler.start();
        
        UARTConfig config = new UARTConfig( 
        	  "ttyAMA0", 1, 9600,
            UARTConfig.DATABITS_8,
            UARTConfig.PARITY_NONE,
            UARTConfig.STOPBITS_1,
            UARTConfig.FLOWCONTROL_NONE
         );
        
        UARTConsole console = new UARTConsole(config);
        
        for(ControlPoint point: pointHandler.getControlPoints()){
            point.addObserver(console);
         }
        
        console.run();
        
        pointHandler.stop();
    }
}