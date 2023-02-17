package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovSlavic extends MarkovGenerator {
	public MarkovSlavic() {
		this(3, new Random());
	}

	public MarkovSlavic(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovSlavic(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("slavic_given.txt", seqlen, rng);
	}

	@Override
	public String feminize(String element, boolean flag) {
		String lastChar = element.substring(element.length() - 1);
		if (element.endsWith("o")) {
			element = element.substring(0, element.length() - 1) + "a";
		} else if (!lastChar.endsWith("a")) {
			element += "a";
		}
		return element;
	}

	@Override
	public String fetch(int gender) {
		String seq1 = this.markov.generateWord();
		if (gender == 0) {
			gender = (this.rng.nextBoolean() ? 1 : 2);
		}
		if (gender == 2) {
			seq1 = this.feminize(seq1, false);
		}
		return seq1;
	}
}
