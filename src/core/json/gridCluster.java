package core.json;

public class gridCluster {

	private String tag;
	private double similarity;
	
	public gridCluster(String tag, double similarity) {
		super();
		this.tag = tag;
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
	

}
