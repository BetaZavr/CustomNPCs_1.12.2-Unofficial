package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovCustomNPCsClassic extends MarkovGenerator {
	public MarkovCustomNPCsClassic() {
		this(3, new Random());
	}

	public MarkovCustomNPCsClassic(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovCustomNPCsClassic(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("customnpcs_classic.txt", seqlen, rng);
	}

	@Override
	public String fetch(int gender) {
		return this.markov.generateWord();
	}
}
