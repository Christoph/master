package tags;

import java.util.Collection;
import java.util.List;

import de.umass.lastfm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Core {


  public static void main(String[] args) {
    // Initializing variables
    Collection<Tag> tags;
    String[] line;
    List<String> lines;
    InputStream input = null;

    // Load config files
    Properties dbconf = new Properties();
    try {

      input = new FileInputStream("config.db");
      dbconf.load(input);

    } catch (IOException e) { e.printStackTrace(); }

    // Initializing classes 
    DB db = new DB(dbconf);
    LastFM last = new LastFM();
    ImportCSV data = new ImportCSV();
      
    // Program
    System.out.println("Start");

    // Import data
    lines = data.importCSV();
    
    for(int i = 0; i< 10; i++)
    {
    line = lines.get(i).split(",");

    tags = last.mineTags(line[0], line[1]);
    db.insert(line[0],line[1],tags);
    
    // Wait to stay below 5 cals per second.
    try {
		Thread.sleep(250); } catch (InterruptedException e) { e.printStackTrace();
	}
    }

    // Close all
    db.closeAll();

    // End
    System.out.println("End");
  }
}
