package core.json;

public class gridCluster {

	private String tag;
	private double importance;
	private double similarity;
	
	public gridCluster(String tag, double importance, double similarity) {
		super();
		this.tag = tag;
		this.importance = importance;
		this.similarity = similarity;
	}

	public gridCluster(String tag) {
		super();
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double getSimilarity() {
		return similarity;
	}


	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	

	public double getImportance() {
		return importance;
	}

	public void setImportance(double importance) {
		this.importance = importance;
	}
}
