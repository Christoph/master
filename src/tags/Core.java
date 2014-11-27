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
    // Initializing classes and variables
    Collection<Tag> tags;
    String[] line;
    List<String> lines;

    Properties dbconf = new Properties();
    InputStream input = null;
    ImportCSV data = new ImportCSV();

    // Load config files
    try {

      input = new FileInputStream("config.db");
      dbconf.load(input);

    } catch (IOException e) { e.printStackTrace(); }

    DB db = new DB(dbconf);
    LastFM last = new LastFM();
      
    // Program
    System.out.println("Start");

    lines = data.importCSV();
    
    tags = last.mineTags("Metallica", "Nothing Else Matters");
    db.insert("Nothing Else Matters","Metallica",tags);
      
    System.out.println("End");
  }
}
