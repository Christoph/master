package mining;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import de.umass.lastfm.*;

public class DBImport {
	//Initialize logger
	private Logger log = Logger.getLogger("Logger");
	
	//Initializing variables
	private String connectionString, user, pass;
	private Connection conn;
	private int longTag = 0;

	private Collection<Tag> tags;
	private Track minedtrack;
	private String[] line;
	private List<String> lines;
	private int counter = 0;
	private int missingTracks = 0;
	private int noTags = 0;
	private int maxTries = 2;
	private int currentTries = 0;
	private Boolean retry = true;
	private String artist, track;
	private long time, starttime;

	//150 is the size of the cell in the table
	int maxString = 150;

	// Initialize classes
	LastFM last;
	ImportCSV data = new ImportCSV();

	private QueryManager querymanager;

	public DBImport(Properties config) {
		last = new LastFM(config);

		connectionString = config.getProperty("database") + "?characterEncoding=utf8";
		user = config.getProperty("user");
		pass = config.getProperty("password");

		try {
			conn = DriverManager.getConnection(connectionString, user, pass);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		querymanager = new QueryManager(conn);
	}

	public void mineAndImportCSV() {
		// Import data
		lines = data.importCSV("db/complete.csv");

		// Starting time
		starttime = System.currentTimeMillis();

		for (String l : lines) {
			// Set and reset variables.
			counter++;
			currentTries = 0;
			retry = true;

			// Extract the artist and track.
			line = l.split(",");

			if (line.length < 2) {
				log.severe("Artist or Track from file missing.");
				line = new String[]{"", ""};
			}

			artist = line[0].trim();
			track = line[1].trim();

			// Check if the names smaller than the db cell.
			if (artist.length() <= maxString && track.length() <= maxString) {
				// Retries three times.
				while (retry) {
					// Mine and insert with exception handling.
					try {
						tags = last.mineTags(track, artist);
						minedtrack = last.mineTrackinfo(track, artist);

						insert(track, artist, tags, minedtrack);

						retry = false;
					} catch (NoTagsException e) {
						retry = false;
						noTags++;

						// Removed because of logfile spamming
						// log.warning("No Tags at Row: "+counter+"; Artist: "+artist+"; Track: "+track);
					} catch (Exception e) {
						if (currentTries >= maxTries) {
							retry = false;
							missingTracks++;

							log.severe(e.getMessage() + "at Row: " + counter + "; Artist: " + artist + "; Track: " + track);
							e.printStackTrace();
						}
					}

					time = System.currentTimeMillis() - starttime;

					// Check if near 5 minutes the cals exceed 1500 with security buffers
					if (time < 300000 && last.getCounter() > 1400) {
						try {
							Thread.sleep(310000 - time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						starttime = System.currentTimeMillis();
						last.setCounter(0);
					}

					if (time >= 300000) {
						starttime = System.currentTimeMillis();
						last.setCounter(0);
					}

					// To keep the cals at bay.
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// Increase the current retry counter.
					currentTries++;
				}

				// Log message all 100 tracks
				if (counter % 100 == 0) {
					log.info("Imported " + counter + " rows; " + "Tracks without tags: " + noTags + " Missing Tracks: " + missingTracks + " Too long Tags: " + getTooLongTags());
				}
			} else {
				missingTracks++;
			}
		}

		//Finished with for loop
		log.info("Imported " + counter + " rows; " + "Tracks without tags: " + noTags + " Missing Tracks: " + missingTracks + " Too long Tags: " + getTooLongTags());
	}

	private void insert(String track, String artist, Collection<Tag> tags, Track minedTrack) throws SQLException {
		int playcount = minedTrack.getPlaycount();
		int listeners = minedTrack.getListeners();

		for (Tag t : tags) {
			if (t.getName().length() <= 150) {
				// Check if the artist exists
				if (!querymanager.existsArtist(artist)) {
					querymanager.insertArtist(artist);
				}

				// Check if the track exists
				if (!querymanager.existsTrack(track, artist)) {


					querymanager.insertTrack(track, artist, listeners, playcount);
				}

				// Check if the tag exists
				if (!querymanager.existsTag(t)) {
					querymanager.insertTag(t);
				}

				// Check if the tag/track combination exists
				if (!querymanager.existsTT(track, artist, t)) {
					querymanager.insertTT(track, artist, t);
				}
			} else {
				log.info("Too long Tag: " + t.getName() + " From Track: " + track + " Artist: " + artist);
				longTag++;
			}
		}

		//Committing the changes
		conn.commit();
	}

	private int getTooLongTags() {
		return longTag;
	}

	public void closeAll() {
		try {
			// Close statements
			querymanager.closeAll();
			// Close connection
			conn.close();
		} catch (SQLException e) {
			log.severe("Error while closing all connections." + e.getSQLState() + e.getMessage());

			e.printStackTrace();
		}
	}
}
