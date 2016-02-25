package processing;

public class StringLengthComparator implements java.util.Comparator<String> {
	public int compare(String o2, String o1) {
		return Integer.compare(o1.length(), o2.length());
	}
}