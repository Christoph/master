package core.json;

public class gridOverview {

	public String tag;
	public double importance;
	public String carrier;
	public int id;
	
	public gridOverview(String tag, double importance, String carrier, int id) {
		super();
		this.tag = tag;
		this.importance = importance;
		this.carrier = carrier;
		this.id = id;
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
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
