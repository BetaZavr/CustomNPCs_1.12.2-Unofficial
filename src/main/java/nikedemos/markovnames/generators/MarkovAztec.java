package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovAztec extends MarkovGenerator {

	public MarkovAztec(int seqlen) {
		markov = new MarkovDictionary("aztec_given.txt", seqlen);
	}

	@Override
	public String fetch(int gender) { return markov.generateWord(); }

}
