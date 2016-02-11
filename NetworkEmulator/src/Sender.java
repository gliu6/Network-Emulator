import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;




public class Sender {
	private Socket socket;
	private String host = "127.0.0.1";
	private int port = 5000; 
	private DataOutputStream os;
	int numOfTries = 0;
	
	BufferedReader br;
	
	Timer timer;
	int NACK = -1;
	int packet = 1;
	int numOfPackets = 100;
	
	public Sender() throws UnknownHostException, IOException {
		super();
		socket = new Socket(host, port);
		System.out.println("Connection established");
		os = new DataOutputStream(socket.getOutputStream());
		//is = socket.getInputStream();
		// TODO Auto-generated constructor stub
	}
	
	
	
	public class listenACKThread extends Thread {

		public void run() {
	    	try {
	    		NACK = br.read();
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
			try {
				if(packet<= numOfPackets){
					os.write(packet);
					os.flush();
					numOfTries++;
					System.out.println("Sender sends pkt: " + packet);  
					packet++;
					
					if(NACK != -1){
						System.out.println("Sender receives NACK: " + NACK);
						System.out.println("retransmiting starts from: " + NACK);
						packet = NACK;
						NACK = -1;
						listenACKThread t1 = new listenACKThread();
						t1.setDaemon(true);
						t1.start();
					}	
				}
				else{
//					System.out.println("send nothing");
					if(NACK != -1){
						System.out.println("Sender receives NACK: " + NACK);
						System.out.println("retransmiting starts from: " + NACK);
						packet = NACK;
						NACK = -1;
						listenACKThread t1 = new listenACKThread();
						t1.setDaemon(true);
						t1.start();
					}	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }     
    }   
	
	

	public void send() throws IOException, InterruptedException {
		
		long start = System.currentTimeMillis();
		
		
		//start NACK listening thread
		NACK = -1;
		listenACKThread t = new listenACKThread();
//		t.setDaemon(true);
		t.start();
		System.out.println("Listening to NACK...");
		
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new SendingTask(), 0, 10);
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
//		Timer NACKTimer = new Timer();
		while(true){
			if(packet>=numOfPackets*3){
				timer.cancel();
				break;
			}
			
		}
		
		long end = System.currentTimeMillis(); 
		System.out.println("Time spent: " + (end - start) + " .Number of tries: " + numOfTries + " Throughput: " + (double)numOfPackets/(end - start)*1000 );  
	}
	
	public void close() throws IOException{
		socket.close();
		System.out.println("Connection closed");
	}
	
//	public class TimeoutTask extends TimerTask {  
//		long threadId;
//		long invokeThreadId;
//		
//        public TimeoutTask(long threadId, long invokeThreadId) {
//			super();
//			this.threadId = threadId;
//			this.invokeThreadId = invokeThreadId;
//		}
//
//		public void run() {     
//            System.out.println("Timeout, resend");
//            Thread t = getThread(this.threadId);
//			System.out.println("Current thread: " + t.getId());
//			t.interrupt();
//			if(t.isInterrupted()){
//				System.out.println(t.getId() + " is interrupted");
//			}
//			Thread t1 = getThread(this.invokeThreadId);
//			t1.notify();
//			System.out.println("invoked thread status: " + t1.isAlive());
//            //timer.cancel();     
//        }     
//    }     
//	
//	public Thread getThread( final long id ) {
//	    final Thread[] threads = getAllThreads( );
//	    for ( Thread thread : threads )
//	        if ( thread.getId( ) == id )
//	            return thread;
//	    return null;
//	}
//	
//	
//	public Thread[] getAllThreads( ) {
//	    final ThreadGroup root = getRootThreadGroup( );
//	    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
//	    int nAlloc = thbean.getThreadCount( );
//	    int n = 0;
//	    Thread[] threads;
//	    do {
//	        nAlloc *= 2;
//	        threads = new Thread[ nAlloc ];
//	        n = root.enumerate( threads, true );
//	    } while ( n == nAlloc );
//	    return java.util.Arrays.copyOf( threads, n );
//	}
//	
//	public ThreadGroup getRootThreadGroup( ) {
////	    if ( rootThreadGroup != null )
////	        return rootThreadGroup;
//	    ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
//	    ThreadGroup ptg;
//	    while ( (ptg = tg.getParent( )) != null )
//	        tg = ptg;
//	    return tg;
//	}
	
	public static void main(String argv[]) throws Exception{
		Sender sender = new Sender();
		sender.send();
		sender.close();
	}
	
}
