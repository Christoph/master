package core.json;

public class gridOverview {

	public String tag;
	public double weight;
	public String item;
	
	public gridOverview(String tag, String carrier, double importance) {
		super();
		this.tag = tag;
		this.weight = importance;
		this.item = carrier;
	}

	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

}
