package core.tags;

public class TagLast extends Tag {
	private int tagWeight;
	private int listeners;
	private int playcount;
	private int ArtistID;
	
	public TagLast(int TTID, String tagName, String originalTagName, int playcount, int tagID, double importance, int LastFMWeight, int songID, String songName, int listeners, int ArtistID ) {
		super(TTID, songName, tagName, originalTagName, songID, tagID, importance);
		this.playcount = playcount;
		this.listeners = listeners;
		this.ArtistID = ArtistID;
		this.tagWeight = LastFMWeight;
	}
	
	public TagLast(TagLast t) {
		super(t);
		
		this.playcount = t.getPlaycount();
		this.listeners = t.getListeners();
		this.ArtistID = t.getArtistID();
		this.tagWeight = t.getTagWeight();
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

	public int getTagWeight() {
		return tagWeight;
	}

	public void setTagWeight(int tagWeight) {
		this.tagWeight = tagWeight;
	}
}
