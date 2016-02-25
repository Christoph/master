package processing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneticStringSimilarity {
	public Pattern remove_duplicate_chars;
	public Matcher remove;
	
	public PhoneticStringSimilarity() {
		remove_duplicate_chars = Pattern.compile("(.)\\1+");
		remove = remove_duplicate_chars.matcher("");
	}

	public String refinedSoundex(String s) {
		char[] ca = s.toCharArray();

		for (int i = 1; i < ca.length; i++) {
			ca[i] = rSTable(ca[i]);
		}

		s = new String(ca);

		// Remove duplicate characters
		s = remove.reset(s).replaceAll("$1");

		// Remove all zeros
		// Better performance than the regex stuff
		s = s.replace("0", "");

		if (s.length() >= 4) {
			// Use only the first 4 characters
			s = s.substring(0, 4);
		} else {
			// Fill missing chars with zeros
			for (int i = s.length(); i < 4; i++) {
				s = s.concat("0");
			}
		}

		return s;
	}

	private char rSTable(char c) {
		// Get the code
		switch (c) {
			case 'a':
			case 'e':
			case 'h':
			case 'i':
			case 'o':
			case 'u':
			case 'w':
			case 'y':
				return '0';
			case 'b':
			case 'p':
				return '1';
			case 'f':
			case 'v':
				return '2';
			case 'c':
			case 'k':
			case 's':
				return '3';
			case 'g':
			case 'j':
				return '4';
			case 'q':
			case 'x':
			case 'z':
				return '5';
			case 'd':
			case 't':
				return '6';
			case 'l':
				return '7';
			case 'm':
			case 'n':
				return '8';
			case 'r':
				return '9';
			default:
				return 'E';
		}
	}
}
