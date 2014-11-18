package tags;

import java.sql.*;

public class QueryManager {
  private ResultSet result;

  // Need to be closed in the closeAll method
  private PreparedStatement selectName;
  private PreparedStatement insertIntoArtist;

  // Constructor
  public QueryManager(Connection conn) {
    try {
      // Initialize prepared statments
      selectName = conn.prepareStatement("SELECT * FROM Artist WHERE Name = ?");
      insertIntoArtist = conn.prepareStatement("INSERT INTO Artist VALUES(DEFAULT,?);");
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public Boolean existArtist(String artist)
  {
	Boolean check = null;
	  
    try {
      //Execute the query
      selectName.setString(1,artist);
      result = selectName.executeQuery();
      
      if(result.next()){
    	check = true;
      }
      else
      {
    	check = false;
      }

      // Close resultset
      result.close();
    } catch (SQLException e) { e.printStackTrace(); }

    // Return the name
    return check;
  }

  public void insertIntoArtist(String name)
  {
    try {
     insertIntoArtist.setString(1,name);
     insertIntoArtist.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void closeAll()
  {
    try {
      // Close all prepared statements
      selectName.close();
      insertIntoArtist.close();
    } catch (SQLException e) { e.printStackTrace(); }
  }
}
