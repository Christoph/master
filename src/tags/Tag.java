package tags;

public class Tag {
	private String tagName;
	private int tagID;
	private int tagWeight;
	private int songID;
	private String songName;
	private int listeners;
	private int playcount;
	
	private String originalTagName;
	
	public Tag(String tagName, int playcount) {
		this.tagName = tagName;
		this.playcount = playcount;
		this.originalTagName = tagName;
		this.tagID = 0;
		this.tagWeight = 0;
		this.songID = 0;
		this.songName = "";
		this.listeners = 0;
	}
	
	public Tag(String tagName, int playcount, int tagID, int tagWeight, int songID, String songName, int listeners ) {
		this.tagName = tagName;
		this.playcount = playcount;
		this.originalTagName = tagName;
		this.tagID = tagID;
		this.tagWeight = tagWeight;
		this.songID = songID;
		this.songName = songName;
		this.listeners = listeners;
	}
	
	public String getTagName() {
		return tagName;
	}
	
	public void setTagName(String name) {
		this.tagName =  name;
	}
	
	public int getTagWeight() {
		return tagWeight;
	}
	
	public String getOriginalTagName() {
		return originalTagName;
	}

	public int getTagID() {
		return tagID;
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
}
