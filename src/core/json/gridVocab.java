package core.json;

public class gridVocab {

	private String tag;
	private double importance;
	
	public gridVocab(String tag, double importance) {
		super();
		this.tag = tag;
		this.importance = importance;
	}
	
	public double getImportance() {
		return importance;
	}
	
	public void setImportance(double importance) {
		this.importance = importance;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	

}
