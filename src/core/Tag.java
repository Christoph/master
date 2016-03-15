package core;

public class Tag {
	private int id;
	private String item;
	private String tag;
	private double weight;
	private double importance;
	private int hasChanged = 0;

	public Tag(int id, String item, String name, double weight, double importance, int hasChanged) {
		super();
		this.id = id;
		this.item = item;
		this.tag = name;
		this.weight = weight;
		this.importance = importance;
		this.hasChanged = hasChanged;
	}

	public Tag(Tag t) {
		super();
		this.id = t.getId();
		this.item = t.getItem();
		this.tag = t.getTag();
		this.weight = t.getWeight();
		this.importance = t.getImportance();
		this.hasChanged = t.getChanged();
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

	public int getChanged() {
		return hasChanged;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {

		if(hasChanged == 0)
		{
			if(!this.tag.equals(tag)) {
				hasChanged = 1;
			}
		}

		this.tag = tag;
	}
}
