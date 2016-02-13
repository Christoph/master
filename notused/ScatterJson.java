package server;

public class ScatterJson {

	private String text;
	private int x;
	private int y;
	
	public ScatterJson(String text, int x, int y) {
		super();
		this.text = text;
		this.x = x;
		this.y = y;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}
	
	
}
