package server;

import com.corundumstudio.socketio.*;

public class Server {
    public void start() throws Exception {
    	
    	// Initialize variables
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        final SocketIOServer server = new SocketIOServer(config);
    	Transport transport = new Transport(server);
    	
    	// Start transport layer
    	transport.initialize();
        
    	// Start server
        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}