package zvieratko;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ZvieratkoServer implements Runnable {

	final static int port = 12345;

	final static int SEE_ANIMAL = 65;      // 'A'
	final static byte CAMERA_SHOT = 67;  // 'C'
	final static byte SOUND_SIGNAL = 68;      // 'D'
	final static byte SOUND_AND_CAMERA_SHOT = 69;  // 'E'
	final static byte ALARM_SIGNAL = 70;  // 'F'
        final static byte PING_SIGNAL = 71;  // 'G'
	
        final static byte COMMUNICATING_THREAD = 1;
        final static byte PINGING_THREAD = 2;

	private Zvieratko zvieratko;
	
	private ArrayList<SocketChannel> connected = null;

        private volatile boolean communicating_thread_runs = false;
        private volatile int type_of_thread;
    
	public ZvieratkoServer(Zvieratko z)
	{
		zvieratko = z;
		connected = new ArrayList<>();
                type_of_thread = COMMUNICATING_THREAD;
		new Thread(this).start();
                while (!communicating_thread_runs);
                type_of_thread = PINGING_THREAD;
		new Thread(this).start();
	}
   
    public void run()
    {
       if (type_of_thread == COMMUNICATING_THREAD) communicating_thread_run();
       else pinging_thread_run();
    }

    void pinging_thread_run()
    {
       try { 
         Thread.sleep(5000); 
         while (communicating_thread_runs)
         {
            emit(PING_SIGNAL);
            Thread.sleep(2000);
         }
       } catch (InterruptedException ee) {}
    }

    void communicating_thread_run()
    {
        Socket socket;	
		    
        try {
            // test if another instance is already running on this host
            socket = new Socket("localhost", port);
            System.out.println("Server is already running");
            System.exit(1);
        } catch (Exception e) {}
            
        // otherwise create a listening socket and wait for the ESP boards to connect
        System.out.println("Server started. Listening for clients.");
        try {
            Selector selector = Selector.open();
            ServerSocketChannel ssc1 = ServerSocketChannel.open();
            ssc1.configureBlocking( false );
            ServerSocket ss = ssc1.socket();
            InetSocketAddress address = new InetSocketAddress( port );
            ss.bind( address );
            
            SelectionKey key1 = ssc1.register( selector, SelectionKey.OP_ACCEPT );
            communicating_thread_runs =  true;

            while (true)
            {
                // wait for a new connection or new request
                int num = selector.select();
                
                Set selectedKeys = selector.selectedKeys();
                Iterator it = selectedKeys.iterator();

                while (it.hasNext()) 
                {
                  SelectionKey key = (SelectionKey)it.next();
                  if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT)
                  {
                      System.out.println("New ESP connecting.");
                      ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                      SocketChannel sc = ssc.accept();	 
					  connected.add(sc);					  
                      sc.configureBlocking( false );
                      SelectionKey newKey = sc.register( selector, SelectionKey.OP_READ );                     
                  }
                  else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) 
                  {
                      // data from ESP
                      SocketChannel sc = (SocketChannel)key.channel();
                      ByteBuffer rb = ByteBuffer.allocate(1);
					  try {
                        sc.read(rb);
                      } catch (IOException e)
					  {
						  connected.remove(sc);
						  it.remove();
						  continue;
					  }
					  rb.flip();
                                          try {
					    if (SEE_ANIMAL == rb.get())
					    {
						zvieratko.seeAnimalEvent();
					    }
                                         } catch (BufferUnderflowException e) {}
                  }                  
                  it.remove();                  
                }
            }
        } catch (UnknownHostException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
        communicating_thread_runs = false;
   }

    public synchronized void emit(byte signal)
	{
		ArrayList<SocketChannel> toRemove = new ArrayList<>();
		
		try {
			for (SocketChannel c : connected)
			{
				if (c.isConnected())
				{
					ByteBuffer wb = ByteBuffer.allocate(10);
					wb.put(signal);
					wb.flip();
					c.write(wb);
				}
                                else toRemove.add(c);
			}
		} catch (Exception e) {
			System.out.println("Warning: Could not talk to remote ESP: " + e);
		}
		for (SocketChannel c : toRemove)
			connected.remove(c);
	}
}
