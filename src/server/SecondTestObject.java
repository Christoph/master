package server;

public class SecondTestObject {

	private String text;
	private int number;
	
	public SecondTestObject()
	{
		
	}

	public SecondTestObject(String text, int number)
	{
		super();
		
		this.text = text;
		this.number = number;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	
}
