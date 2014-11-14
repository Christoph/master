package tags;

import java.sql.*;
import java.util.Collection;

import org.apache.commons.dbutils.*;

import de.umass.lastfm.*;

public class DB {
	private String connectionString = "jdbc:mariadb://localhost/tags";
	private String user = "root";
	private String pass = "sh0wt1me";
	private Connection conn;
	private String query;
	private QueryRunner run;
	
	public void insert(Collection<Tag> tags) {
		//Initialization
		 run = new QueryRunner();
	
		for(Tag t: tags)
		{
			// Check
			// TO BE DONE
			
			// Build query
			query = "INSERT INTO Tag VALUES(DEFAULT,'"+t.getName()+"');";
			
			// Execute the query
			try {   	
				conn = DriverManager.getConnection(connectionString, user, pass);
				run.update(conn, query);
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(conn);
			}
		}
	}
}
