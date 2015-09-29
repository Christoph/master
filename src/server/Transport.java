package server;

import java.util.List;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;

import processing.Workflow;

public class Transport {

	private Workflow work;
	private SocketIOServer server;
	
	protected Transport(Workflow work, SocketIOServer server) {
		super();
		this.work = work;
		this.server = server;
	}

	public void initialize()
	{
		server.addEventListener("test", SecondTestObject.class, new DataListener<SecondTestObject>() {

			public void onData(SocketIOClient arg0, SecondTestObject data,
					AckRequest arg2) throws Exception {
				arg0.sendEvent("response", data.getText().toUpperCase());
				
				System.out.println("Event 0");
				System.out.println(data.getText());
				System.out.println(data.getNumber());
				
			}
        });
        
        server.addEventListener("json", SecondTestObject.class, new DataListener<SecondTestObject>() {

			public void onData(SocketIOClient arg0, SecondTestObject data,
					AckRequest arg2) throws Exception {
				
				System.out.println("start");
				arg0.sendEvent("response", "started");
				
				List<String> temp = work.full();

				arg0.sendEvent("response", temp.toString());
					
				System.out.println("finished");
				arg0.sendEvent("response", "finished");
				
			}
        });
	}
}
