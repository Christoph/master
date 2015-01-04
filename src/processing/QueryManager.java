package processing;

import java.sql.*;
import java.util.logging.Logger;

public class QueryManager {
	// Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
	// Important querys
	// Gives how many tracks have 1, 2, 3 .... tags.
	private String getListOfTagsPerTrack = "select tags,count(*) from Track inner join (select Track.ID,count(*) as tags from TT inner join Track on TT.TrackID = Track.ID group by Track.ID) as t on Track.ID = t.ID group by tags";
	
  // Need to be closed in the closeAll method
  private PreparedStatement selectIDFromArtist;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize preparedstatments
      selectIDFromArtist = conn.prepareStatement("SELECT ID FROM Artist WHERE Name = ?");
      
    } catch (SQLException e) { 
    	log.severe("Error in the DB constructor."+e.getSQLState()+e.getMessage());
    	
    	e.printStackTrace(); 
    }
  }

  public Boolean existsArtist(String artist) throws SQLException
  {
    Boolean check = false;
    ResultSet result;

      // Execute the query
      selectIDFromArtist.setString(1,artist);
      result = selectIDFromArtist.executeQuery();
      
      // Check if the row exists
      if(result.next()){
      check = true;
      }
      
      // Close resultset
      result.close();

    return check;
  }
  
  public void closeAll() throws SQLException
  {
      // Close all prepared statements
      selectIDFromArtist.close();
  }
}
