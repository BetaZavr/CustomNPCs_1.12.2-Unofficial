package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import noppes.npcs.config.TrueTypeFont;

public class TextContainer {

	public Pattern regexString = Pattern.compile("([\"'])(?:\\\\.|[^\"'])*?\\1", Pattern.MULTILINE);
	public Pattern regexFunction = Pattern.compile("\\b(if|else|switch|with|for|while|in|var|const|let|throw|then|function|continue|break|foreach|return|try|catch|finally|do|this|typeof|instanceof|new)(?=\\W)");
	public Pattern regexWord = Pattern.compile("[\\p{L}-]+|\\n|$");
	public Pattern regexNumber = Pattern.compile("\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?[fFbBdDlLsS]?|NaN|null|Infinity|unidentified|true|false)\\b");
	public Pattern regexComment = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*|#.*");
	public String text;
	public List<MarkUp> makeup = new ArrayList<>();
	public List<LineData> lines = new ArrayList<>();
	public int visibleLines = 1;
	public int lineHeight;
	public int totalHeight;
	public int linesCount;

	public TextContainer(String mainText) {
		text = mainText.replaceAll("\\r?\\n|\\r", "\n");
	}

	public boolean compareMarkUps(MarkUp mu1, MarkUp mu2) {
		return mu1 == null || mu1.start > mu2.start;
	}

	public void formatCodeText() {
		MarkUp markup;
		for (int start = 0; (markup = this.getNextMatching(start)) != null; start = markup.end) {
			this.makeup.add(markup);
		}
	}

	private MarkUp getNextMatching(int start) {
		MarkUp markup = null;
		String s = this.text.substring(start);
		Matcher matcher = this.regexNumber.matcher(s);
		if (matcher.find()) {
			markup = new MarkUp(matcher.start(), matcher.end(), '6', 0);
		}
		matcher = this.regexFunction.matcher(s);
		if (matcher.find()) {
			MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '2', 0);
			if (this.compareMarkUps(markup, markup2)) {
				markup = markup2;
			}
		}
		matcher = this.regexString.matcher(s);
		if (matcher.find()) {
			MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '4', 7);
			if (this.compareMarkUps(markup, markup2)) {
				markup = markup2;
			}
		}
		matcher = this.regexComment.matcher(s);
		if (matcher.find()) {
			MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '8', 7);
			if (this.compareMarkUps(markup, markup2)) {
				markup = markup2;
			}
		}
		if (markup != null) {
            markup.start += start;
            markup.end += start;
		}
		return markup;
	}

	public void init(TrueTypeFont font, int width, int height) {
		if (lineHeight == 0) { lineHeight = 12; }
		int totalChars = 0;
		for (String l : text.split("\n")) {
			StringBuilder line = new StringBuilder();
			Matcher m = regexWord.matcher(l);
			int i = 0;
			while (m.find()) {
				String word = l.substring(i, m.start());
				if (font.width(line + word) > width - 10) {
					lines.add(new LineData(line.toString(), totalChars, totalChars + line.length()));
					totalChars += line.length();
					line = new StringBuilder();
				}
				if (font.width(word) > width - 10) {
					StringBuilder w = new StringBuilder();
					for (int c = 0; c < word.length(); c++) {
						if (font.width(w.toString() + word.charAt(c)) <= width - 10) { w.append(word.charAt(c)); }
						else {
							lines.add(new LineData(w.toString(), totalChars, totalChars + w.length()));
							totalChars += w.length();
							line = new StringBuilder();
							w = new StringBuilder("" + word.charAt(c));
						}
					}
					line.append(w);
				} else {
					line.append(word);
				}
				i = m.start();
			}
			lines.add(new LineData(line.toString(), totalChars, totalChars + line.length() + 1));
			totalChars += line.length() + 1;
		}
		this.linesCount = this.lines.size();
		this.totalHeight = this.linesCount * this.lineHeight;
		this.visibleLines = Math.max(height / this.lineHeight, 1);
	}

}
