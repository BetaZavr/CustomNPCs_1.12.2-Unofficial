package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovWelsh extends MarkovGenerator {

	public MarkovDictionary markov2;

	public MarkovWelsh(int seqlen) {
		markov = new MarkovDictionary("welsh_male.txt", seqlen);
		markov2 = new MarkovDictionary("welsh_female.txt", seqlen);
	}

	@Override
	public String fetch(int gender) {
		if (gender == 0) { gender = (MarkovDictionary.rnd.nextBoolean() ? 1 : 2); }
		return gender == 2 ? markov2.generateWord() : markov.generateWord();
	}

}
