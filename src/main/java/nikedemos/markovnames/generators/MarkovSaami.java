package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovSaami extends MarkovGenerator {

	public MarkovSaami(int seqlen) {
		markov = new MarkovDictionary("saami_bothgenders.txt", seqlen);
	}

	@Override
	public String fetch(int gender) { return markov.generateWord(); }

}
