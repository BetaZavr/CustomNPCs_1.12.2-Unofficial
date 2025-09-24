package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovRoman extends MarkovGenerator {

	public MarkovDictionary markov2;
	public MarkovDictionary markov3;

	public MarkovRoman(int seqlen) {
		markov = new MarkovDictionary("roman_praenomina.txt", seqlen);
		markov2 = new MarkovDictionary("roman_nomina.txt", seqlen);
		markov3 = new MarkovDictionary("roman_cognomina.txt", seqlen);
	}

	@Override
	public String feminize(String element, boolean flag) {
		if (element.endsWith("us") || element.endsWith("o")) { return element.substring(0, element.length() - 2) + "a"; }
		return element;
	}

	@Override
	public String fetch(int gender) {
		String seq1 = markov.generateWord();
		String seq2 = markov2.generateWord();
		String seq3 = markov3.generateWord();
		if (gender == 0) { gender = (MarkovDictionary.rnd.nextBoolean() ? 1 : 2); }
		if (gender == 2) {
			seq1 = feminize(seq1, false);
			seq2 = feminize(seq2, false);
			seq3 = feminize(seq3, true);
		}
		return seq1 + " " + seq2 + " " + seq3;
	}

}
