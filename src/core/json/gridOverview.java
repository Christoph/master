package core.json;

public class gridOverview {

	public String tag;
	public double importance;
	public String item;
	
	public gridOverview(String tag, String carrier, double importance) {
		super();
		this.tag = tag;
		this.importance = importance;
		this.item = carrier;
	}

	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double getImportance() {
		return importance;
	}

	public void setImportance(double importance) {
		this.importance = importance;
	}

	public String getCarrier() {
		return item;
	}

	public void setCarrier(String carrier) {
		this.item = carrier;
	}

}
