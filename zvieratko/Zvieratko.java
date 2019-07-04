package zvieratko;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import purejavacomm.*;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

public class Zvieratko extends JFrame
{
	private static final int BAUD_RATE = 115200;
	
	private static final int WAKEUP_SOUND = 50;
	
	OutputStream arduino;
	SerialPort serialPort = null;
	InputStream response;
	ZvieratkoServer zs = null;
	
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(() -> {
			new Zvieratko(args).initUI();
		});		
	}
	
	public Zvieratko(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("usage example:  zviaratko COM3");
			System.exit(1);
		}
        
		CommPortIdentifier portId = null;
	
		try {
          portId = CommPortIdentifier.getPortIdentifier(args[0]);
        } catch (Exception e) {
			System.out.println("Could not open serial port to arduino (" + args[0] + "): " + e);
			System.exit(1);
		}
		try {
		  serialPort = (SerialPort) portId.open("Zvieratko", 2000);
		  serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		}  catch (Exception e) {
			System.out.println("Opening serial port to arduino not possible, isn't it already used by another application?  " + e);
			System.exit(1);
		}
		
		try {
		  arduino = serialPort.getOutputStream();
		  response = serialPort.getInputStream();
	
		}
		catch (IOException e) {
			System.out.println("Could not open serial port to arduino for writing: " + e);
			try { arduino.close(); } catch (Exception e4) {}
			serialPort.close();
			System.exit(1);
		}
		
		try {
	      Thread.sleep(500);
		  
		  arduino.write(49);
		  arduino.flush();
		  Thread.sleep(2000);
		  int x = response.read();
		  if (x != 49)
		  {
			  System.out.println("No response from Arduino, is it connected?");
			  arduino.close();
			  response.close();
			  serialPort.close();
			  System.exit(1);
		  }
		} catch (Exception e3) {
			System.out.println("Problem talking to Arduino: " + e3);
			System.exit(1);
		}
		
		zs = new ZvieratkoServer(this);
	}
	
	private void initUI()
	{
		
		setTitle("Zvieratko");
        setSize(160, 700);
		setLocation(1200, 20);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeThisApp();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout(FlowLayout.LEFT, 20, 60) );
        
		JButton b = new JButton("Alarm");
		b.setPreferredSize(new Dimension(100, 100));
		panel.add(b);
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("sending alarm...");
				zs.emit(ZvieratkoServer.ALARM_SIGNAL);
			}
		});

		b = new JButton("Sound");
		b.setPreferredSize(new Dimension(100, 100));
		panel.add(b);
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zs.emit(ZvieratkoServer.SOUND_SIGNAL);
			}
		});

		b = new JButton("Camera");
		b.setPreferredSize(new Dimension(100, 100));
		panel.add(b);
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zs.emit(ZvieratkoServer.CAMERA_SHOT);
			}
		});
		
		b = new JButton("S + C");
		b.setPreferredSize(new Dimension(100, 100));
		panel.add(b);
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zs.emit(ZvieratkoServer.SOUND_AND_CAMERA_SHOT);
			}
		});

		add(panel);
	    setVisible(true);
	}
	
	public void seeAnimalEvent()
	{
		try {
		  arduino.write(WAKEUP_SOUND);
		}
		catch (Exception e)
		{
			System.out.println("Warning: could not send request to arduino: " + e);
		}
	}
	
	private void closeThisApp()
	{
		System.out.println("Bye.");
		try { serialPort.close(); } catch (Exception e) {}
		System.exit(0);
	}
}
