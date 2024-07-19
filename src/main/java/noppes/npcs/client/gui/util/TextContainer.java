package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import noppes.npcs.config.TrueTypeFont;

public class TextContainer {

	public Pattern regexString;
	public Pattern regexFunction;
	public Pattern regexWord;
	public Pattern regexNumber;
	public Pattern regexComment;
	public String text;
	public List<MarkUp> makeup;
	public List<LineData> lines;
	public int lineHeight;
	public int totalHeight;
	public int visibleLines;
	public int linesCount;

	public TextContainer(String text) {
		this.regexString = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1", Pattern.MULTILINE);
		this.regexFunction = Pattern.compile("\\b(if|else|switch|with|for|while|in|var|const|let|throw|then|function|continue|break|foreach|return|try|catch|finally|do|this|typeof|instanceof|new)(?=[^\\w])");
		this.regexWord = Pattern.compile("[\\p{L}-]+|\\n|$");
		this.regexNumber = Pattern.compile("\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|unidentified|true|false)\\b");
		this.regexComment = Pattern.compile("\\/\\*[\\s\\S]*?(?:\\*\\/|$)|\\/\\/.*|#.*");
		this.makeup = new ArrayList<>();
		this.lines = new ArrayList<>();
		this.visibleLines = 1;
		(this.text = text).replaceAll("\\r?\\n|\\r", "\n");
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

	public String getFormattedString() {
		StringBuilder builder = new StringBuilder(this.text);
		for (MarkUp entry : this.makeup) {
			builder.insert(entry.start, '\uffff' + Character.toString(entry.c));
			builder.insert(entry.end, '\uffff' + Character.toString('r'));
		}
		return builder.toString();
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
		if (this.lineHeight == 0) {
			this.lineHeight = 12;
		}
		String[] split = this.text.split("\n");
		int totalChars = 0;
		for (String l : split) {
			StringBuilder line = new StringBuilder();
			Matcher m = this.regexWord.matcher(l);
			int i = 0;
			while (m.find()) {
				String word = l.substring(i, m.start());
				if (font.width(line + word) > width - 10) {
					this.lines.add(new LineData(line.toString(), totalChars, totalChars + line.length()));
					totalChars += line.length();
					line = new StringBuilder();
				}
				line.append(word);
				i = m.start();
			}
			this.lines.add(new LineData(line.toString(), totalChars, totalChars + line.length() + 1));
			totalChars += line.length() + 1;
		}
		this.linesCount = this.lines.size();
		this.totalHeight = this.linesCount * this.lineHeight;
		this.visibleLines = Math.max(height / this.lineHeight, 1);
	}

}
