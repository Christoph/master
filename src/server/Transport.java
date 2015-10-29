package server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import processing.WorkflowLast;
import processing.WorkflowMovie;

public class Transport {

	private WorkflowLast work = new WorkflowLast();
	//private WorkflowMovie work = new WorkflowMovie();
	private SocketIOServer server;
	private Boolean initialized = false;
	
	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	public void initialize()
	{
		work.init();
		//work.nlpPipeline();
		work.grouping();
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				System.out.println("Connected to :"+client.getSessionId());
			}
		});
		
		// Get data
		server.addEventListener("initialize", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(!initialized)
				{
					// Initialize data
					work.init();
					initialized = true;
				}
				
				System.out.println("update start");
				
				client.sendEvent("data", work.getJSON());
				
				System.out.println("update end");
			}
        });
        
        // Run 
        server.addEventListener("run", String.class, new DataListener<String>() {

			@Override
			public void onData(SocketIOClient client, String data, AckRequest arg2)
					throws Exception {
				
				work.nlpPipeline();
				
				// Redraw all
				client.sendEvent("data", work.getJSON());
			}
		});
	}
}
