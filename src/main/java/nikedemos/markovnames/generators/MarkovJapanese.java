package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovJapanese extends MarkovGenerator {
	public MarkovDictionary markov2;
	public MarkovDictionary markov3;

	public MarkovJapanese() {
		this(4, new Random());
	}

	public MarkovJapanese(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovJapanese(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("japanese_surnames.txt", seqlen, rng);
		this.markov2 = new MarkovDictionary("japanese_given_male.txt", seqlen, rng);
		this.markov3 = new MarkovDictionary("japanese_given_female.txt", seqlen, rng);
	}

	@Override
	public String fetch(int gender) {
		StringBuilder name = new StringBuilder(this.markov.generateWord());
		name.append(" ");
		if (gender == 0) {
			gender = (this.rng.nextBoolean() ? 1 : 2);
		}
		if (gender == 2) {
			name.append(this.markov3.generateWord());
		} else {
			name.append(this.markov2.generateWord());
		}
		return name.toString();
	}
}
