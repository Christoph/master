package server;

public class FilterJson {

	private String chartDiv;
	private double lower;
	private double upper; 

	public FilterJson()
	{

	}
	
	public FilterJson(double lower, double upper, String chartDiv)
	{
		super();
		
		this.chartDiv = chartDiv;
		this.lower = lower;
		this.upper = upper;
	}

	public String getChartDiv() {
		return chartDiv;
	}

	public void setChartDiv(String chartDiv) {
		this.chartDiv = chartDiv;
	}

	public double getLower() {
		return lower;
	}

	public void setLower(double lower) {
		this.lower = lower;
	}

	public double getUpper() {
		return upper;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}
	

	
	
}
