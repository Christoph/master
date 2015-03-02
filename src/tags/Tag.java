package tags;

public class Tag {
	private String name;
	private int count;
	private String original;
	
	public Tag(String name, int count) {
		this.name = name;
		this.count = count;
		this.original = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name =  name;
	}
	
	public int getCount() {
		return count;
	}
	
	public String getOriginal() {
		return original;
	}
}
