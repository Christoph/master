package server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import processing.lastFM.WorkflowLast;

public class Transport {

	//private WorkflowAbstract work = new WorkflowAbstract();
	private WorkflowLast work = new WorkflowLast();
	//private WorkflowMovie work = new WorkflowMovie();
	private SocketIOServer server;
	
	protected Transport(SocketIOServer server) {
		super();
		this.server = server;
	}

	public void initialize()
	{		
		work.init();
		work.nlpPipeline();
		//work.grouping();
		//work.regex();
		
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
				
				System.out.println(data);
				
				client.sendEvent("data", "data");
			}
        });
	}
}
