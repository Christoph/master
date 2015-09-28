package server;

import java.util.ArrayList;
import java.util.List;

import processing.Workflow;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.protocol.PacketEncoder;
import com.corundumstudio.socketio.protocol.PacketType;
import com.corundumstudio.socketio.*;

import core.Tag;

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
        
        final Workflow work = new Workflow();
        
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
        
        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}