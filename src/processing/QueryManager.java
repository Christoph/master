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
  private PreparedStatement deleteTrackWithLessThanTags;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize preparedstatments
    	// Deletes all tracks with less than ? tags.
      deleteTrackWithLessThanTags = conn.prepareStatement("delete Track from Track inner join (select Track.ID,count(*) as tags from TT inner join Track on TT.TrackID = Track.ID group by Track.ID) as t on Track.ID = t.ID where tags < ?");
      
    } catch (SQLException e) { 
    	log.severe("Error in the DB constructor."+e.getSQLState()+e.getMessage());
    	
    	e.printStackTrace(); 
    }
  }

  public void deleteTracksWithTagsLessThan(int number) throws SQLException
  {
      // Execute the query
      deleteTrackWithLessThanTags.setInt(1,number);
      deleteTrackWithLessThanTags.executeQuery();
  }
  
  public void closeAll() throws SQLException
  {
      // Close all prepared statements
      deleteTrackWithLessThanTags.close();
  }
}
