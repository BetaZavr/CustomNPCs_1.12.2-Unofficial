package nikedemos.markovnames.generators;

import java.util.Random;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovGenerator {

	public MarkovDictionary markov;
	public String name;
	public Random rng;
	public String symbol;

	public MarkovGenerator() {
		this(3, new Random());
	}

	public MarkovGenerator(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovGenerator(int seqlen, Random rng) {
		this.rng = rng;
	}

	public String feminize(String element, boolean flag) {
		return element;
	}

	public String fetch() {
		return this.fetch(0);
	}

	public String fetch(int gender) {
		return this.stylize(this.markov.generateWord());
	}

	public String stylize(String str) {
		return str;
	}
}
