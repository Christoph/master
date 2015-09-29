package server;

import processing.Workflow;
import com.corundumstudio.socketio.*;


public class Server {
    public void start() throws Exception {
    	
    	// Initialize variables
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        final SocketIOServer server = new SocketIOServer(config);
    	Workflow work = new Workflow();
    	Transport transport = new Transport(work, server);
    	
    	// Start transport layer
    	transport.initialize();
        
    	// Start server
        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}