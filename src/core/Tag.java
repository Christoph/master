package core;

public class Tag {
	private int TTID;
	private String tagName;
	private int tagID;
	private int lastFMWeight;
	private int songID;
	private String songName;
	private int listeners;
	private int playcount;
	private double weight;
	private int ArtistID;
	
	private String originalTagName;
	
	public Tag(String tagName, int listeners) {
		this.tagName = tagName;
		this.listeners = listeners;
		this.originalTagName = tagName;
		this.tagID = 0;
		this.lastFMWeight = 0;
		this.songID = 0;
		this.songName = "";
		this.playcount = 0;
		this.TTID = 0;
		this.ArtistID = 0;
	}
	
	public Tag(int TTID, String tagName, int playcount, int tagID, int tagWeight, int songID, String songName, int listeners, int ArtistID ) {
		this.TTID = TTID;
		this.tagName = tagName;
		this.playcount = playcount;
		this.originalTagName = tagName;
		this.tagID = tagID;
		this.lastFMWeight = tagWeight;
		this.songID = songID;
		this.songName = songName;
		this.listeners = listeners;
		this.ArtistID = ArtistID;
	}
	
	public Tag(int TTID, String tagName, int playcount, int tagID, double importance, int LastFMWeight, int songID, String songName, int listeners, int ArtistID ) {
		this.TTID = TTID;
		this.tagName = tagName;
		this.playcount = playcount;
		this.originalTagName = tagName;
		this.tagID = tagID;
		this.weight = importance;
		this.songID = songID;
		this.songName = songName;
		this.listeners = listeners;
		this.ArtistID = ArtistID;
		this.lastFMWeight = LastFMWeight;
	}
	
	public String getTagName() {
		return tagName;
	}
	
	public void setTagName(String name) {
		this.tagName =  name;
	}
	
	public int getLastFMWeight() {
		return lastFMWeight;
	}
	
	public String getOriginalTagName() {
		return originalTagName;
	}

	public int getTagID() {
		return tagID;
	}
	
	public int getTTID() {
		return TTID;
	}
	
	public void setTTID(int ID) {
		this.TTID = ID;
	}

	public int getArtistID() {
		return ArtistID;
	}
	
	public void setTagID(int tagID) {
		this.tagID = tagID;
	}

	public int getSongID() {
		return songID;
	}

	public void setSongID(int songID) {
		this.songID = songID;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public int getListeners() {
		return listeners;
	}

	public void setListeners(int listeners) {
		this.listeners = listeners;
	}

	public int getPlaycount() {
		return playcount;
	}

	public void setPlaycount(int playcount) {
		this.playcount = playcount;
	}

	public double getImportance() {
		return weight;
	}

	public void setImportance(double weight) {
		this.weight = weight;
	}
}
