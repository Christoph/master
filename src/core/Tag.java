package core;

public class Tag {
	private int id;
	private String item;
	private String tag;
	private double weight;
	private double importance;

	public Tag(int id, String item, String name, double weight, double importance) {
		super();
		this.id = id;
		this.item = item;
		this.tag = name;
		this.weight = weight;
		this.importance = importance;
	}

	public Tag(Tag t) {
		super();
		this.id = t.getId();
		this.item = t.getItem();
		this.tag = t.getTag();
		this.weight = t.getWeight();
		this.importance = t.getImportance();
	}
	
	public String getItem() {
		return item;
	}

	public double getImportance() {
		return importance;
	}

	public void setImportance(double importance) {
		this.importance = importance;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
