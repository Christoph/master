package server;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;

public class Server {
    public void start() throws Exception {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);

        final SocketIOServer server = new SocketIOServer(config);

        server.addEventListener("test", TestObject.class, new DataListener<TestObject>() {

			public void onData(SocketIOClient arg0, TestObject data,
					AckRequest arg2) throws Exception {
				arg0.sendEvent("response", data.getText().toUpperCase());
				
				System.out.println(data.getText());
				
			}
        });
        
        
        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}