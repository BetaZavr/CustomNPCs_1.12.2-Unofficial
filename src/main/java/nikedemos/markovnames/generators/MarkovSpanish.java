package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;
import nikedemos.markovnames.MarkovDictionarySPA;

public class MarkovSpanish extends MarkovGenerator {

	public MarkovDictionary markov2;
	public MarkovDictionary markov3;

	public MarkovSpanish(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovSpanish(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("spanish_given_male.txt", seqlen, rng);
		this.markov2 = new MarkovDictionary("spanish_given_female.txt", seqlen, rng);
		this.markov3 = new MarkovDictionarySPA("spanish_surnames.txt", seqlen, rng);
	}

	@Override
	public String fetch(int gender) {
		String giv;
		String sur = this.markov3.generateWord();
		if (gender == 0) {
			gender = (this.rng.nextBoolean() ? 1 : 2);
		}
		if (gender == 1) {
			giv = this.markov.generateWord();
		} else {
			giv = this.markov2.generateWord();
		}
		return giv + " " + sur;
	}
}
