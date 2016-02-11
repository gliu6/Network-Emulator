import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;




public class Receiver {
	private int port = 6000;
	private ServerSocket serverSocket;
	private Socket socket;
	private DataOutputStream os;
	private int numOfPackets = 100;
	private int numOfTries = 0;
	private int lastSet = 0; 
	
	public Receiver() throws UnknownHostException, IOException {
		super();
		serverSocket = new ServerSocket(port);
		// TODO Auto-generated constructor stub
	}
	
	
public class TimeoutTask extends TimerTask {  
		
		int timouOutPacket = 0;
        public TimeoutTask(int pkt) {
			super();
			timouOutPacket = pkt;
		}

		public void run() {     
//			System.out.println("Timeout, request packet: " + timouOutPacket);
			System.out.flush();
			int NACK =  timouOutPacket;
			try {
				os.write(NACK);
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    }   
	
	public void receive() throws IOException, InterruptedException {
		
		long start = System.currentTimeMillis();
		
		socket = null;
		socket = serverSocket.accept();
		os = new DataOutputStream(socket.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		int packet = 0;
		int previousPacket = 0;
		
		
		Timer timeout = null ;
		while((packet = br.read())!=-1){
			//get the packet in order
			
			if(packet==lastSet&&previousPacket==packet-1){
				timeout.cancel();
			}
			if(packet==numOfPackets&&previousPacket==numOfPackets-1){
				System.out.println("Receiver receives pkt: " + packet);
				System.out.flush();
				break;
			}
			for(int j=1; j< 6; j++){
				if(packet==numOfPackets - j&&previousPacket == numOfPackets-j-1){
					lastSet = numOfPackets - j+1;
					timeout = new Timer();
					timeout.schedule(new TimeoutTask(packet+1), 40);
				}
			}
			
			if(packet == previousPacket + 1){
				System.out.println("Receiver receives pkt: " + packet);
				System.out.flush();
				previousPacket = packet;
				
				//test
//				if(packet == 7||packet ==10){
//					System.out.println("Receiver receives disordered pkt: " + packet);
//					System.out.flush();
//					int NACK =  previousPacket+1;
//					os.write(4);
//					os.flush();
//				}
			//get the packet, but not in order. Then reply NACK
			}else if(packet >previousPacket+1){
				System.out.println("Receiver receives disordered pkt: " + packet);
				System.out.flush();
				int NACK =  previousPacket+1;
				os.write(NACK);
				os.flush();
			}
		}
		
		long end = System.currentTimeMillis(); 
		System.out.println("Time spent: " + (end - start) + " Throughput: " + (double)numOfPackets/(end - start)*1000 );  
	
	}
	
	public void close() throws IOException{
		socket.close();
		serverSocket.close();
		System.out.println("Connection closed");

	}
	
	
	public static void main(String argv[]) throws Exception{
		Receiver receiver = new Receiver();
		receiver.receive();
		receiver.close();
	}
	
}
