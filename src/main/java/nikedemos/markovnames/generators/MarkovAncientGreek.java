package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovAncientGreek extends MarkovGenerator {
	public MarkovDictionary markov2;

	public MarkovAncientGreek() {
		this(3, new Random());
	}

	public MarkovAncientGreek(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovAncientGreek(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("ancient_greek_male.txt", seqlen, rng);
		this.markov2 = new MarkovDictionary("ancient_greek_female.txt", seqlen, rng);
	}

	@Override
	public String fetch(int gender) {
		if (gender == 0) {
			gender = (this.rng.nextBoolean() ? 1 : 2);
		}
		String seq1;
		if (gender == 2) {
			seq1 = this.markov2.generateWord();
		} else {
			seq1 = this.markov.generateWord();
		}
		return seq1;
	}
}
