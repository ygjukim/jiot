package com.example.cli;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.util.Observable;
import java.util.Observer;

import com.example.thing.ControlPoint;

import jdk.dio.DeviceManager;
import jdk.dio.uart.UART;
import jdk.dio.uart.UARTConfig;

public class UARTConsole implements Observer{

    private UART uart;
    private BufferedReader in;
    private BufferedWriter out;

    public UARTConsole(UARTConfig config) throws IOException {
        uart = (UART) DeviceManager.open(config);
        in = new BufferedReader(new InputStreamReader(
                Channels.newInputStream(uart)));
        out = new BufferedWriter(new OutputStreamWriter(
                Channels.newOutputStream(uart)));
        uart.setReceiveTimeout(100);
    }

    public void run() throws IOException {
        System.out.println("Waiting command...");
        write("Please input command:");
        
        for (String line = in.readLine();
                line == null || !line.equals("quit");
                line = in.readLine()) {
            if(line == null)
                continue;
            
            System.out.println("Received message: " + line);
            String[] command = line.split(" ");
            String result;
            try{
                result = Commander.getInstance().execute(command);
            }catch(Throwable ex){
                result = "Exception happend: " + ex.getMessage();
            }
            
            if(result != null)
                write(result);
        }

        write("Good bye!");
        close();
    }

    @Override
    public void update(Observable ob, Object arg) {
        if(ob instanceof ControlPoint){
        	ControlPoint point = (ControlPoint) ob;
            if(arg == null){
                write("Changed of value (" + point.getName() + "): "
                    + point.getPresentValue());
            }else{
                if (arg.toString().equals("name")) {
                		write("Changed name (" + point.getName() + "): " + point.getName());
                }
                else {
                		write("Changed (" + point.getName() + "): " + arg);
                }
            }
        }
    }

    private void write(String result) {
        try {
            out.write(result);
            out.newLine();
            out.flush();
        } catch (IOException ex) {

        }
    }

    private void close() throws IOException {
        in.close();
        out.close();
        uart.close();
    }
}
