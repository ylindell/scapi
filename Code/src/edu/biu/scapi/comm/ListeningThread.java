/**
 * 
 */
package edu.biu.scapi.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/** 
 * @author LabTest
 */
public class ListeningThread extends Thread{
	private Map<InetAddress , SecuringConnectionThread> connectingThreads;//map that includes only SecuringConnectionThread of the down connections
	private int port;//the port to listen on
	private boolean bStopped = false;//a flag that indicates if to keep on listening or stop
	private ServerSocketChannel listener;
	

	/**
	 * 
	 */
	public ListeningThread( Map<InetAddress ,SecuringConnectionThread> securingThreads, int port) {

		connectingThreads = securingThreads;
		
		//prepare the listener.
		try {
			listener = ServerSocketChannel.open();
			listener.socket().bind (new InetSocketAddress (port));
			//listener.configureBlocking (false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 * stopConnecting - sets the flag bStopped to false. In the run function of this thread this flag is checked
	 * 					if the flag is true the run functions returns, otherwise continues.
	 */
	public void stopConnecting(){
		
		//set the flag to true.
		bStopped = true;
	}
	
	
	
	/**
	 * run : This function is the main function of the ListeningThread. Mainly, we listen and accept valid connections as long
	 *  	 as the flag bStopped is false.
	 *       We use the ServerSocketChannel rather than the regular ServerSocket since we want the accept to be non-blocking. If
	 *       the accept function is blocking the flag bStopped will not be checked until the thread is unblocked.  
	 */
	public void run() {

		//first set the channels in the map to connecting
		Collection<SecuringConnectionThread> c = connectingThreads.values();
		Iterator<SecuringConnectionThread> itr = c.iterator();
		
		while(itr.hasNext()){  
			Channel ch = ((SecuringConnectionThread)itr.next()).getChannel();
			
			if(ch instanceof PlainChannel)
		       ((PlainChannel)ch).setState(edu.biu.scapi.comm.State.CONNECTING);
		       
		}
		
		int numOfIncomingConnections = connectingThreads.size();
			
		//loop for incoming connections and make sure that this thread should not stopped.
        for (int i = 0; i < numOfIncomingConnections && !bStopped; i++) {
        	
            SocketChannel socketChannel = null;
			try {
				
				//use the server socket to listen on incoming connections.
				// accept connections from all the smaller processes
				
				System.out.println("Trying to listen " + listener.socket().getLocalPort());
				socketChannel = listener.accept();
				
				//s.setTcpNoDelay(true);//consider the 2 options of nagle
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//there was no connection request
			if(socketChannel==null){
				i--;//iterate back since there was no connection request
				try {
					Thread.sleep (1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				//get the address from the socket and find it the map
				SecuringConnectionThread scThread = connectingThreads.get(socketChannel.socket().getLocalAddress());
				
				//check if the ip address is a valid address. i.e. exists in the map
				if(scThread==null){//an un authorized ip tried to connect
					i--; ////iterate back since no legal ip has connected
				}
	        	else{ //we have a thread that corresponds to this ip address. Thus, this address is valid
	        		
	        		//check that the channel is concrete channel and not some decoration
	        		if(scThread.getChannel() instanceof PlainTCPChannel){
	        			//get the channel from the thread and set the obtained socket.
	        			((PlainTCPChannel)scThread.getChannel()).setSocket(socketChannel.socket());
	        			
	        			//start the connecting thread
	        			scThread.start();
	        		}
	        		else
	        			;//throw an exception. The channel must be concrete
	        		
	        	}
			}
        		
        }	
        System.out.println("End of listening thread run");
	}
}