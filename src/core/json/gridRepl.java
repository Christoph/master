package core.json;

public class gridRepl {

	private String truth;
	private String replacement;
	private double similarity;
	public String getTruth() {
		return truth;
	}
	public void setTruth(String truth) {
		this.truth = truth;
	}
	public String getReplacement() {
		return replacement;
	}
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
	public double getSimilarity() {
		return similarity;
	}
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	public gridRepl(String truth, String replacement, double similarity) {
		super();
		this.truth = truth;
		this.replacement = replacement;
		this.similarity = similarity;
	}

}
