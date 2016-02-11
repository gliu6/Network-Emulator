import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;





public class Router {
	private Socket revSocket;
	private Socket sendSocket;
	private String host = "127.0.0.1";
	private int port1 = 5000;
	private int port2 = 6000; 
	private DataOutputStream outToSender;
	private DataOutputStream outToReceiver;
	private ServerSocket serverSocket;
	//private InputStream is;
	
	int NACK = -1;
	BufferedReader brAck; 
	ArrayList<Integer> queue = new ArrayList<Integer>();
	int numOfPackets = 0;
	int minThreshold = 0;
	int maxThreshold = 40;
	
	public Router() throws UnknownHostException, IOException {
		super();
		sendSocket = new Socket(host, port2);
		System.out.println("Connection to receiver established");
		outToReceiver = new DataOutputStream(sendSocket.getOutputStream());
		serverSocket = new ServerSocket(port1);
		queue.clear();
	}
	
	public class listenACKThread extends Thread {

		public void run() {
	    	try {
	    		NACK = brAck.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	public class SendingTask extends TimerTask {  
		
        public SendingTask() {
			super();
		}

		public void run() {     
			if(queue.size()!=0){
				//get the head packet and send
				int sendingPacket = queue.get(0);
				queue.remove(0);
				try {
					outToReceiver.write(sendingPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("sending packet: " + sendingPacket);
			}
			
			if(NACK != -1){
				System.out.println("Router receives NACK and relay to Sender: " + NACK);
				//forward the ack
				try {
					outToSender.write(NACK);
					outToSender.flush();
					NACK = -1;
					listenACKThread t1 = new listenACKThread();
					t1.setDaemon(true);
					t1.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}     
    }   
	
	public void start() throws IOException, InterruptedException {
		
		revSocket = null;
		revSocket = serverSocket.accept();
		outToSender = new DataOutputStream(revSocket.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(revSocket.getInputStream()));
		
		
		//start NACK listening thread
		NACK = -1;
		listenACKThread t = new listenACKThread();
		t.setDaemon(true);
		t.start();
		System.out.println("Listening to NACK from receiver...");
		
		int packet = -1;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new SendingTask(), 0, 20);
		brAck = new BufferedReader(new InputStreamReader(sendSocket.getInputStream()));
		
		
		while((packet = br.read())!=-1){
			
			
//			System.out.println("Router receives from sender pkt: " + packet);
			System.out.flush();
			
			//forward the packet
			
			double p = 1.0*queue.size()/maxThreshold;
			if(p>=1){
				p = 1;
			}
			double r = Math.random();
			if(r<=p){
				//drop the packet
				System.out.println("drop packet: " + packet);
			}
			else{
				//queue the packet
				if(queue.size()<maxThreshold){
					queue.add(packet);
					System.out.println("Queue packet: "+ packet + " " +  "buffer size: " + queue.size());
				}
			}
			
				
			
				
		}
		
	}
	
	public void close() throws IOException{
		sendSocket.close();
		revSocket.close();
		serverSocket.close();
		System.out.println("Connection closed");
	}
	
	
	public static void main(String argv[]) throws Exception{
		Router router = new Router();
		router.start();
		router.close();
	}
	
}
