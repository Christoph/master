package core.json;

public class gridGroup {

	private String group;
	private double strength;
	
	public gridGroup(String group, double strength) {
		super();
		this.group = group;
		this.strength = strength;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public double getStrength() {
		return strength;
	}
	public void setStrength(double strength) {
		this.strength = strength;
	}


}
