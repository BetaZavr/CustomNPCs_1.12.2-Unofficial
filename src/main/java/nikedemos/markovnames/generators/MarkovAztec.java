package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovAztec extends MarkovGenerator {
	public MarkovAztec() {
		this(3, new Random());
	}

	public MarkovAztec(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovAztec(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("aztec_given.txt", seqlen, rng);
	}

	@Override
	public String fetch(int gender) {
		return this.markov.generateWord();
	}
}
