package core.json;

public class gridHist {

	private double value;
	private long count;
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public gridHist(double value, long count) {
		super();
		this.value = value;
		this.count = count;
	}

}
