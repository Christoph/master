package core.json;

import java.util.List;

public class gridHistory {

	public String original;
	public String pre;
	public String spell;
	public String composite;
	public String post;

	public gridHistory(String original, String spell, String pre, String composite,
	                   String post) {
		super();
		this.original = original;
		this.spell = spell;
		this.pre = pre;
		this.composite = composite;
		this.post = post;
	}

	public gridHistory(List<String> history) {
		// Fill up unused History slots
		while (history.size() < 5) history.add("");
		
		this.original = history.get(0);
		this.spell = history.get(1);
		this.pre = history.get(2);
		this.composite = history.get(3);
		this.post = history.get(4);
	}
}
