package core;

public class Tag {
	private int ID;
	private String carrierName;
	private String tagName;
	private int carrierID;
	private int tagID;
	private double importance;
	
	public Tag(int ID, String carrier, String tagName, int carrierID,
			int tagID, double importance) {
		super();
		this.ID = ID;
		this.carrierName = carrier;
		this.tagName = tagName;
		this.carrierID = carrierID;
		this.tagID = tagID;
		this.importance = importance;
	}
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrier) {
		this.carrierName = carrier;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public int getCarrierID() {
		return carrierID;
	}
	public void setCarrierID(int carrierID) {
		this.carrierID = carrierID;
	}
	public int getTagID() {
		return tagID;
	}
	public void setTagID(int tagID) {
		this.tagID = tagID;
	}
	public double getImportance() {
		return importance;
	}
	public void setImportance(double importance) {
		this.importance = importance;
	}
	
}
