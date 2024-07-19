package nikedemos.markovnames;

import java.util.HashMap;
import java.util.Map;

import nikedemos.markovnames.generators.MarkovAncientGreek;
import nikedemos.markovnames.generators.MarkovAztec;
import nikedemos.markovnames.generators.MarkovGenerator;
import nikedemos.markovnames.generators.MarkovJapanese;
import nikedemos.markovnames.generators.MarkovOldNorse;
import nikedemos.markovnames.generators.MarkovRoman;
import nikedemos.markovnames.generators.MarkovSaami;
import nikedemos.markovnames.generators.MarkovSlavic;
import nikedemos.markovnames.generators.MarkovWelsh;

public class Main {

	public static HashMap<String, MarkovGenerator> GENERATORS = new HashMap<>();

	public static void main(String[] args) {
		Main.GENERATORS.put("ROMAN", new MarkovRoman(3));
		Main.GENERATORS.put("JAPANESE", new MarkovJapanese(4));
		Main.GENERATORS.put("SLAVIC", new MarkovSlavic(3));
		Main.GENERATORS.put("WELSH", new MarkovWelsh(3));
		Main.GENERATORS.put("SAAMI", new MarkovSaami(3));
		Main.GENERATORS.put("OLDNORSE", new MarkovOldNorse(4));
		Main.GENERATORS.put("ANCIENTGREEK", new MarkovAncientGreek(3));
		Main.GENERATORS.put("AZTEC", new MarkovAztec(3));
		for (Map.Entry<String, MarkovGenerator> pair : Main.GENERATORS.entrySet()) {
			System.out.println("===" + pair.getKey() + "===");
			for (int i = 0; i < 16; ++i) {
				if (i == 0) {
					System.out.println("GENTLEMEN-----------");
				}
				int gender = (i < 8) ? 1 : 2;
				String random_name = pair.getValue().fetch(gender);
				System.out.println(random_name);
				if (i == 15) {
					System.out.println("\n");
				} else if (i == 7) {
					System.out.println("LADIES--------------");
				}
			}
		}
	}

}
