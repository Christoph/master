package core.json;

public class gridSpell {

	private String tag;
	private String replacement;
	private double importanceTag;
	private double importanceReplacement;
	private double similarity;

	public gridSpell(String tag, String replacement, double importanceTag, double importanceReplacement, double similarity) {
		super();
		this.tag = tag;
		this.replacement = replacement;
		this.importanceTag = importanceTag;
		this.importanceReplacement = importanceReplacement;
		this.similarity = similarity;
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

	public double getImportanceReplacement() {
		return importanceReplacement;
	}

	public void setImportanceReplacement(double importanceReplacement) {
		this.importanceReplacement = importanceReplacement;
	}

	public double getImportanceTag() {
		return importanceTag;
	}

	public void setImportanceTag(double importanceTag) {
		this.importanceTag = importanceTag;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
}
