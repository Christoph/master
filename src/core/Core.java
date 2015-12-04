package core;

import java.io.IOException;
import java.util.logging.*;

import org.eclipse.swt.widgets.Composite;

import server.Server;
import gui.userinterface;;

public class Core {

  private static final Logger log = Logger.getLogger("Logger");

  public static void main(String[] args) {
	  
    ////////////////////////////////////////////////////////////////
    /// Initialization
    ////////////////////////////////////////////////////////////////
	  
	Handler handler;

	try {
      handler = new FileHandler( "log.txt" );
      handler.setFormatter(new SimpleFormatter());
      log.addHandler( handler );
    } catch (SecurityException e1) { e1.printStackTrace();
    } catch (IOException e1) { e1.printStackTrace(); }
	
    log.info("Initializing");
    
    ////////////////////////////////////////////////////////////////
    /// GUI
    ////////////////////////////////////////////////////////////////

	userinterface gui = new userinterface();
	
	gui.open();
    
    ////////////////////////////////////////////////////////////////
    /// SERVER
    ////////////////////////////////////////////////////////////////

    Server server = new Server();

	try {
		//server.start();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
    log.info("END");
  }
}
