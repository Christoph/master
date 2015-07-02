package processing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import tags.Tag;
import tags.TagsToCSV;

public class QueryManager {
	// Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
  // Need to be closed in the closeAll method
  private PreparedStatement deleteTrackWithLessThanTags;
  private PreparedStatement selectTagsWhichOccurMoreThan;
  private PreparedStatement selectTagsWhichOccurLessOrEqualThan;
  private PreparedStatement selectTagsWithCount;
  private PreparedStatement selectAll;
  private Statement stmt;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize preparedstatments
    	// Deletes all tracks with less than ? tags.
      deleteTrackWithLessThanTags = conn.prepareStatement("delete Track from Track inner join (select Track.ID,count(*) as tags from TT inner join Track on TT.TrackID = Track.ID group by Track.ID) as t on Track.ID = t.ID where tags < ?");
      selectTagsWhichOccurMoreThan = conn.prepareStatement("select * from Tag inner join (select TT.TagID,count(*) as n from Tag inner join TT on Tag.ID = TT.TagID group by TT.TagID) as t on Tag.ID = t.TagID Where n > ?");
      selectTagsWhichOccurLessOrEqualThan = conn.prepareStatement("select * from Tag inner join (select TT.TagID,count(*) as n from Tag inner join TT on Tag.ID = TT.TagID group by TT.TagID) as t on Tag.ID = t.TagID Where n <= ?");
      selectTagsWithCount = conn.prepareStatement("select Tag.Name,count(*) as Count from Tag inner join TT on Tag.ID = TT.TagID group by TT.TagID");
      selectAll = conn.prepareStatement("select Track.ID as SongID, Track.Name as SongName, Track.Listeners, Track.Playcount, Tag.ID as TagID, Tag.Name as TagName, TT.Count as TagWeight from TT inner join Track on TT.TrackID = Track.ID inner join Tag on TT.TagID = Tag.ID;");
      selectAll.setFetchSize(10000);
      
      stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
              java.sql.ResultSet.CONCUR_READ_ONLY);
      stmt.setFetchSize(Integer.MIN_VALUE);
      
      
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
  
  public List<String> getTagsWhichOccursMoreThan(int times) throws SQLException {
  	List<String> data = new ArrayList<String>();
  	
  	selectTagsWhichOccurMoreThan.setInt(1,times);
  	ResultSet result = selectTagsWhichOccurMoreThan.executeQuery();
  	
  	while(result.next()) {
  		data.add(result.getString("Name").toLowerCase());
  	}
  	
  	return data;
  }
  
  public List<String> getTagsWhichOccurLessOrEqualThan(int times) throws SQLException {
  	List<String> data = new ArrayList<String>();
  	
  	selectTagsWhichOccurLessOrEqualThan.setInt(1,times);
  	ResultSet result = selectTagsWhichOccurLessOrEqualThan.executeQuery();
  	
  	while(result.next()) {
  		data.add(result.getString("Name").toLowerCase());
  	}

    // Close huge resultset
    result.close();
  	
  	return data;
  }
  
  public List<Tag> getAll() throws SQLException {
  	List<Tag> data = new ArrayList<Tag>();
  	
  	ResultSet result = stmt.executeQuery("select Track.ID as SongID, Track.Name as SongName, Track.Listeners, Track.Playcount, Tag.ID as TagID, Tag.Name as TagName, TT.Count as TagWeight from TT inner join Track on TT.TrackID = Track.ID inner join Tag on TT.TagID = Tag.ID;");
  	
  	while(result.next()) {    		
  		data.add(new Tag(result.getString("TagName").toLowerCase().replace('-', ' '), result.getInt("Playcount"), result.getInt("TagID"), result.getInt("TagWeight"), result.getInt("SongID"),result.getString("SongName").toLowerCase(), result.getInt("Listeners")));
  	}

  	System.out.print("size:");
  	System.out.println(data.size());
  	
    // Close huge resultset
    result.close();
  	
  	return data;
  }
  
  public void exportAll(String file) throws SQLException {
  	TagsToCSV writer = new TagsToCSV(file);
  	
  	ResultSet result = selectAll.executeQuery();
  	
  	while(result.next()) {  
  		writer.writeTag(new Tag(result.getString("TagName").toLowerCase(), result.getInt("Playcount"), result.getInt("TagID"), result.getInt("TagWeight"), result.getInt("SongID"),result.getString("SongName").toLowerCase(), result.getInt("Listeners")));	
  	}

  	writer.closeWriteTag();
  	result.close();
  }
  
  public void closeAll() throws SQLException
  {
      // Close all prepared statements
      deleteTrackWithLessThanTags.close();
      selectTagsWhichOccurMoreThan.close();
      selectTagsWhichOccurLessOrEqualThan.close();
      selectTagsWithCount.close();
      selectAll.close();
  }
}
