package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovSaami extends MarkovGenerator {
	public MarkovDictionary markov2;

	public MarkovSaami() {
		this(3, new Random());
	}

	public MarkovSaami(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovSaami(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("saami_bothgenders.txt", seqlen, rng);
	}

	@Override
	public String fetch(int gender) {
		return this.markov.generateWord();
	}
}
