package tags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.*;

import mining.*;
import processing.*;

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
    InputStream input = null;

    // Load config files
    Properties dbconf = new Properties();
    try {

      input = new FileInputStream("config.db");
      dbconf.load(input);

    } catch (IOException e) { e.printStackTrace(); }

    ////////////////////////////////////////////////////////////////
    /// DATA IMPORT
    ////////////////////////////////////////////////////////////////
    log.info("Import");
    
    DBImport dbi = new DBImport(dbconf);
    
    dbi.mineAndImportCSV();

    // Close all
    dbi.closeAll();
    
    log.info("Import Finished");
    
    ////////////////////////////////////////////////////////////////
    /// DATA Processing
    ////////////////////////////////////////////////////////////////
    log.info("Data Processing");
    
    Processor pro = new Processor(dbconf);
    
		pro.deleteTracksWithTagsLessThan(5);
    
    // Close all
    pro.closeAll();
    
    log.info("END");
  }
}
