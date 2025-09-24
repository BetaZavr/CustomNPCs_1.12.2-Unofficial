package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovCustomNPCsClassic extends MarkovGenerator {

	public MarkovCustomNPCsClassic(int seqlen) {
		markov = new MarkovDictionary("customnpcs_classic.txt", seqlen);
	}

	@Override
	public String fetch(int gender) { return markov.generateWord(); }

}
