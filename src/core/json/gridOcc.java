package core.json;

public class gridOcc {

	private String tag;
	private long occurrence;

	public gridOcc(String tag, long occurrence) {
		super();
		this.tag = tag;
		this.occurrence = occurrence;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public long getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(long occurrence) {
		this.occurrence = occurrence;
	}
}
