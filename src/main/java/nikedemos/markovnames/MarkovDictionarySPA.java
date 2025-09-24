package nikedemos.markovnames;

public class MarkovDictionarySPA extends MarkovDictionary {

	public MarkovDictionarySPA(String dictionary, int seqlen) { super(dictionary, seqlen); }

	public String getCapitalizedSPA(String str) {
		String[] parts = str.split("#");
		StringBuilder build = new StringBuilder();
		for (int p = 0; p < parts.length; ++p) {
			if (!parts[p].equals("de") && !parts[p].equals("del") && !parts[p].equals("la") && !parts[p].equals("los")) {
				parts[p] = getCapitalized(parts[p]);
			}
			if (p > 0) { build.append(" "); }
			build.append(parts[p]);
		}
		return build.toString();
	}

	@Override
	public String getPost(String str) { return getCapitalizedSPA(str); }

}
