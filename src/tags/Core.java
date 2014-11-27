package tags;

import java.util.Collection;
import java.util.List;

import de.umass.lastfm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.*;

public class Core {

  private static final Logger log = Logger.getLogger("Logger");

  public static void main(String[] args) {
  // Initializing the logger
	Handler handler;

	try {
      handler = new FileHandler( "log.txt" );
      handler.setFormatter(new SimpleFormatter());
      log.addHandler( handler );
    } catch (SecurityException e1) { e1.printStackTrace();
    } catch (IOException e1) { e1.printStackTrace(); }
	
    log.info("Initializing");

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

    log.info("Import");
    
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

    log.info("End");
  }
}
