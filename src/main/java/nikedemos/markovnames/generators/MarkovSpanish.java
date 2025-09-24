package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;
import nikedemos.markovnames.MarkovDictionarySPA;

public class MarkovSpanish extends MarkovGenerator {

	public MarkovDictionary markov2;
	public MarkovDictionary markov3;

	public MarkovSpanish(int seqlen) {
		markov = new MarkovDictionary("spanish_given_male.txt", seqlen);
		markov2 = new MarkovDictionary("spanish_given_female.txt", seqlen);
		markov3 = new MarkovDictionarySPA("spanish_surnames.txt", seqlen);
	}

	@Override
	public String fetch(int gender) {
		String sur = markov3.generateWord();
		if (gender == 0) { gender = MarkovDictionary.rnd.nextBoolean() ? 1 : 2; }
		return (gender == 1 ? markov.generateWord() : markov2.generateWord()) + " " + sur;
	}

}
