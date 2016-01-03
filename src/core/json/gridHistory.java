package core.json;

import java.util.List;

public class gridHistory {

	public String original;
	public String pre;
	public String composite;
	public String post;

	public gridHistory(String original, String pre, String composite,
			String post) {
		super();
		this.original = original;
		this.pre = pre;
		this.composite = composite;
		this.post = post;
	}
	public gridHistory(List<String> history) {
		// Fill up unused History slots
		while(history.size() < 4) history.add("");
		
		this.original = history.get(0);
		this.pre = history.get(1);
		this.composite = history.get(2);
		this.post = history.get(3);
	}
	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}
	public String getPre() {
		return pre;
	}
	public void setPre(String pre) {
		this.pre = pre;
	}
	public String getComposite() {
		return composite;
	}
	public void setComposite(String composite) {
		this.composite = composite;
	}
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}

}
