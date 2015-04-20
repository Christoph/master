package tags;

import java.io.File;
import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;

public class ImportCSV {

  public List<String> importCSV(String data)
  {
    List<String> lines = null;
    try {
      File file = new File(data);
      lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    } catch (IOException e) { e.printStackTrace(); }

    return lines;
  }
}