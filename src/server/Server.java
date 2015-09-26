package server;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;
import com.corundumstudio.*;

public class Server {
    public void start() throws Exception {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);

        final SocketIOServer server = new SocketIOServer(config);

        server.addEventListener("test", SecondTestObject.class, new DataListener<SecondTestObject>() {

			public void onData(SocketIOClient arg0, SecondTestObject data,
					AckRequest arg2) throws Exception {
				arg0.sendEvent("response", data.getText().toUpperCase());
				
				System.out.println("Event 0");
				System.out.println(data.getText());
				System.out.println(data.getNumber());
				
			}
        });
        
        server.addEventListener("test1", SecondTestObject.class, new DataListener<SecondTestObject>() {

			public void onData(SocketIOClient arg0, SecondTestObject data,
					AckRequest arg2) throws Exception {
				arg0.sendEvent("response", data.getText().toUpperCase());
				
				System.out.println("Event 1");
				System.out.println(data.getText());
				System.out.println(data.getNumber());
				
			}
        });
        
        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}