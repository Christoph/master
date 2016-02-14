package core.json;

public class gridHistFreq {

	private long value;
	private long count;
	
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	
	public gridHistFreq(long value, long count) {
		super();
		this.value = value;
		this.count = count;
	}

}
