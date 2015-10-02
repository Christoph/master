package core;

public class TagLast extends Tag {
	private int lastFMWeight;
	private int listeners;
	private int playcount;
	private int ArtistID;
	private String originalTagName;
	
	public TagLast(int TTID, String tagName, int playcount, int tagID, double importance, int LastFMWeight, int songID, String songName, int listeners, int ArtistID ) {
		super(TTID, songName, tagName, songID, tagID, importance);
		this.playcount = playcount;
		this.listeners = listeners;
		this.ArtistID = ArtistID;
		this.lastFMWeight = LastFMWeight;
	}
	
	public int getLastFMWeight() {
		return lastFMWeight;
	}
	
	public String getOriginalTagName() {
		return originalTagName;
	}

	public int getArtistID() {
		return ArtistID;
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
}
