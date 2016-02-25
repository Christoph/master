package core;

import java.util.ArrayList;
import java.util.List;

public class Tag {
	private String item;
	private List<String> tag;
	private double weight;
	private double importance;

	public Tag(String item, String name, double weight, double importance) {
		super();
		this.item = item;
		this.tag = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			this.tag.add("");
		}
		this.tag.set(0, name);
		this.weight = weight;
		this.importance = importance;
	}
	
	public Tag(String item, List<String> tag, double weight, double importance) {
		super();
		this.item = item;
		this.tag = tag;
		this.weight = weight;
		this.importance = importance;
	}
	
	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getTag(int index) {
		return tag.get(index);
	}

	public List<String> getTag() {
		return tag;
	}

	public void setTag(int index, String name) {
		tag.set(index, name);
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
}
