package jiot.raspi.thing;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceManager;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.UnsupportedDeviceTypeException;
import jdk.dio.gpio.GPIOPin;

public class AsyncToggleOutputPoint extends OutputControlPoint implements CommandExecutable {
	private int pinId;
	private GPIOPin pinDev;
	
	private Thread toggleThread;
    private Semaphore toggleSem = null;
    private boolean bRun = true;
    private boolean bToggle = false;
	private int interval;
	
	public AsyncToggleOutputPoint(int pinId) {
		super();
		this.pinId = pinId;
		
        bRun = true;
        bToggle = false;
        interval = 500;
        toggleSem = new Semaphore(1);
        try {
			toggleSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	@Override
	public void open() {
		try {
			pinDev = (GPIOPin)DeviceManager.open(pinId, GPIOPin.class);
		} catch (UnsupportedDeviceTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeviceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnavailableDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			setPresentValue(0);
			pinDev.setValue(false);
		} catch (UnavailableDeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClosedDeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		toggleThread = new Thread(new Runnable() {
			@Override
			public void run() {
		        while (bRun) {
		            try {
		            	toggleSem.acquire();
					  } catch (InterruptedException e) {
						  // TODO Auto-generated catch block
						  e.printStackTrace();
					  }
		        		
		            while (bToggle) {
		            	  try {
							  pinDev.setValue(true);
			                Thread.sleep(interval);

			                if (!bRun || !bToggle) {
			                	  pinDev.setValue(false);
			                	  break;
			                  }
			                  
			                pinDev.setValue(false);
			                Thread.sleep(interval);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		             }	// while
		        }		// while		 				
			}			
		});
		
		toggleThread.start();
	}

	@Override
	public void close() {
		if (isEnabled()) {
			if (toggleThread != null) {
				try {
					bRun = false;
					toggleThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				pinDev.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pinDev = null;
		}
	}

	@Override
	public boolean isEnabled() {
		return (pinDev != null && pinDev.isOpen());
	}

	@Override
	public Type getType() {
		return Type.TDO;
	}

	@Override
	public void setPresentValue(int value) {
		bToggle = (value == 1);
		if (bToggle) {	// start toggling
			toggleSem.release();
		}
		presentValue.set(value);
	}

	@Override
	public int executeCommmad(String[] command) {
		if (command.length >= 2) {
			if (command[0] != null && command[0].equals("interval")) {
				setInterval(Integer.parseInt(command[1]));
				return Integer.parseInt(command[1]);
			}
		}
		return 0;
	}

}