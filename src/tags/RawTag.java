package tags;

public class RawTag {
	private String name;
	private int count;
	
	public RawTag(String name, int count) {
		this.name = name;
		this.count = count;
	}
	
	public String getName() {
		return name;
	}
	
	public int getCount() {
		return count;
	}
}
