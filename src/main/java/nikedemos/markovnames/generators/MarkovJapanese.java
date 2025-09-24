package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovJapanese extends MarkovGenerator {

	public MarkovDictionary markov2;
	public MarkovDictionary markov3;

	public MarkovJapanese(int seqlen) {
		markov = new MarkovDictionary("japanese_surnames.txt", seqlen);
		markov2 = new MarkovDictionary("japanese_given_male.txt", seqlen);
		markov3 = new MarkovDictionary("japanese_given_female.txt", seqlen);
	}

	@Override
	public String fetch(int gender) {
		StringBuilder name = new StringBuilder(markov.generateWord());
		name.append(" ");
		if (gender == 0) { gender = (MarkovDictionary.rnd.nextBoolean() ? 1 : 2); }
		return name.append((gender == 2 ? markov3 : markov2).generateWord()).toString();
	}

}
