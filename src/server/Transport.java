package server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import processing.WorkflowLast;

public class Transport {

	private WorkflowLast work;
	private SocketIOServer server;
	
	protected Transport(WorkflowLast work, SocketIOServer server) {
		super();
		this.work = work;
		this.server = server;
	}

	public void initialize()
	{	
		List<String> charts = new ArrayList<String>();
		
		// Define all charts
		charts.add("#hex1");
		charts.add("#hist1");
		
		// Connection
		server.addConnectListener(new ConnectListener() {
			
			public void onConnect(SocketIOClient client) {
				System.out.println("Connected to :"+client.toString());
			}
		});
		
		// Get data
		server.addEventListener("initialize", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data,
					AckRequest arg2) throws Exception {
				
				if(data.equals("init"))
				{					
					// Initialize data
					work.init(charts);
					
					for(String chart: charts)
					{
						System.out.println("update start");
						client.sendEvent(chart, work.updateData(chart));
						System.out.println("update end");
					}
				}
				else
				{
					System.out.println(data);
				}
			}
        });
		
		// Filter
        server.addEventListener("filter", FilterJson.class, new DataListener<FilterJson>() {

			public void onData(SocketIOClient client, FilterJson data,
					AckRequest arg2) throws Exception {
				
				// Redraw all except himself
				for(String chart: charts.stream()
					    .filter(p -> !p.contains(data.getChartDiv()))
					    .collect(Collectors.toList()))
				{
					work.filter(data.getLower(), data.getUpper(), chart, "bla");
					
					client.sendEvent(chart, work.updateData(chart));
				}
			}
        });
	}
}
