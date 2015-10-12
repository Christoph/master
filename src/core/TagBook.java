package core;

public class TagBook extends Tag{
	
	private int tagWeight;
	
	public TagBook(int ID, String title, String tagName, String originalTagName, int bookmarkID,
			int tagID, int tagWeight, double importance) {
		super(ID, title, tagName, originalTagName, bookmarkID, tagID, importance);
		
		this.setTagWeight(tagWeight);
	}

	public int getTagWeight() {
		return tagWeight;
	}

	public void setTagWeight(int tagWeight) {
		this.tagWeight = tagWeight;
	}
}
