package processing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import tags.RawTag;

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

  public void deleteTracksWithTagsLessThan(int number) {
  	try {
  		// Delete Tracks
			querymanager.deleteTracksWithTagsLessThan(number);
		
	    //Committing the changes
	    conn.commit();
  	} catch (SQLException e) {
  		log.severe(e.getMessage());
			e.printStackTrace();
		}
  }
  
  public List<String> getTagsOccuringMoreThan(int times) {
  	List<String> tags = new ArrayList<String>();
  	
  	try {
			tags = querymanager.getTagsWhichOccursMoreThan(times);
  	} catch (SQLException e) {
  		log.severe(e.getMessage());
			e.printStackTrace();
		}
  	
  	return tags;
  }
  
  public List<String> getTagsOccuringLessOrEqualThan(int times) {
  	List<String> tags = new ArrayList<String>();
  	
  	try {
			tags = querymanager.getTagsWhichOccurLessOrEqualThan(times);
  	} catch (SQLException e) {
  		log.severe(e.getMessage());
			e.printStackTrace();
		}
  	
  	return tags;
  }
  
  public List<RawTag> getTagsWithCount() {
  	List<RawTag> tags = new ArrayList<RawTag>();
  	
  	try {
			tags = querymanager.getTagsWithCount();
  	} catch (SQLException e) {
  		log.severe(e.getMessage());
			e.printStackTrace();
		}
  	
  	return tags;
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
