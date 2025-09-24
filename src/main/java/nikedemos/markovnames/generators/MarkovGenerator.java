package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

public class MarkovGenerator {

	public MarkovDictionary markov;
	public String name;
	public String symbol;

	public MarkovGenerator() { this(3); }

	public MarkovGenerator(int ignoredSeqlen) { }

	public String feminize(String element, boolean flag) { return element; }

	public String fetch() { return fetch(0); }

	public String fetch(int gender) { return stylize(markov.generateWord()); }

	public String stylize(String str) { return str; }

}
