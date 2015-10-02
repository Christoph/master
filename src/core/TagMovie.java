package core;

public class TagMovie extends Tag{
	private int rtCriticScore;
	private int rtAudienceScore;
	private int tagWeight;
	
	public TagMovie(int ID, String title, int rtCriticScore,
			int rtAudienceScore, String tagName, int tagID, int movieID,
			int tagWeight, double importance) {
		super(ID, title, tagName, movieID, tagID, importance);
		
		this.rtCriticScore = rtCriticScore;
		this.rtAudienceScore = rtAudienceScore;
		this.tagWeight = tagWeight;
	}

	public int getRtCriticScore() {
		return rtCriticScore;
	}

	public void setRtCriticScore(int rtCriticScore) {
		this.rtCriticScore = rtCriticScore;
	}

	public int getRtAudienceScore() {
		return rtAudienceScore;
	}

	public void setRtAudienceScore(int rtAudienceScore) {
		this.rtAudienceScore = rtAudienceScore;
	}

	public int getTagWeight() {
		return tagWeight;
	}

	public void setTagWeight(int tagWeight) {
		this.tagWeight = tagWeight;
	}
}
