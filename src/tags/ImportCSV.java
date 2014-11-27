package tags;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class ImportCSV {

  public List<String> importCSV()
  {
    List<String> lines = null;
    try {
      File file = new File("db/example_track_artist.csv");
      lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    } catch (IOException e) { e.printStackTrace(); }

    return lines;
  }
}
