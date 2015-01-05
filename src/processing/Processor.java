package processing;

import java.sql.*;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class Processor {
	//Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
  private String connectionString, user, pass;
  private Connection conn;

  private QueryManager querymanager;

  public Processor(Properties config) {
    connectionString = config.getProperty("database");
    user = config.getProperty("user");
    pass = config.getProperty("password");

    try {
		conn = DriverManager.getConnection(connectionString, user, pass);
	} catch (SQLException e) {
		e.printStackTrace();
	}
    querymanager = new QueryManager(conn);
  }

  public void deleteTracksWithTagsLessThan(int number) throws SQLException {
  	querymanager.deleteTracksWithTagsLessThan(number);
  
    //Committing the changes
    conn.commit();
  }

  public void closeAll()
  {
    try {
      // Close statements
      querymanager.closeAll();
      // Close connection
      conn.close();
    } catch (SQLException e) { 
    	log.severe("Error while closing all connections."+e.getSQLState()+e.getMessage());
    	
    	e.printStackTrace(); 
    }
  }
}
