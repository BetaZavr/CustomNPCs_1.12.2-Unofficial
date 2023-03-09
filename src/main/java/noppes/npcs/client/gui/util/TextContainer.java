package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import noppes.npcs.CustomNpcs;
import noppes.npcs.config.TrueTypeFont;

public class TextContainer {
	
	public class LineData {
		public int end;
		public int start;
		public String text;
		private boolean isCode;
		private String coloricChar = Character.toString('\uffff');

		public LineData(String text, int start, int end, boolean bo) {
			this.text = text;
			this.start = start;
			this.end = end;
			this.isCode = bo;
		}

		public String getFormattedString() {
			StringBuilder builder = new StringBuilder(this.text);
			if (!this.isCode) { return builder.toString(); }
			int found = 0;
			for (MarkUp entry : TextContainer.this.makeup) {
				if (entry.start >= this.start && entry.start < this.end) {
					builder.insert(entry.start - this.start + found * 2, this.coloricChar + Character.toString(entry.c));
					++found;
				}
				if (entry.start < this.start && entry.end > this.start) {
					builder.insert(0, this.coloricChar + Character.toString(entry.c));
					++found;
				}
				if (entry.end >= this.start && entry.end < this.end) {
					builder.insert(entry.end - this.start + found * 2, this.coloricChar + Character.toString('r'));
					++found;
				}
			}
			return builder.toString();
		}
	}

	class MarkUp {
		public char c;
		public int end;
		public int level;
		public int start;

		public MarkUp(int start, int end, char c, int level) {
			this.start = start;
			this.end = end;
			this.c = c;
			this.level = level;
		}
	}

	public int lineHeight;
	public List<LineData> lines;
	public int linesCount;
	public List<MarkUp> makeup;
	public Pattern regexComment;
	public Pattern regexFunction;
	public Pattern regexNumber;
	public Pattern regexString;
	public Pattern regexWord;
	public String text;
	public int totalHeight;
	public int visibleLines;
	private String coloricChar = Character.toString('\uffff');

	public TextContainer(String text) {
		this.regexString = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1", 8);
		this.regexFunction = Pattern.compile("\\b(if|else|switch|with|for|while|in|on|var|const|let|throw|then|function|continue|break|foreach|return|try|catch|finally|do|this|typeof|instanceof|new)(?=[^\\w])");
		this.regexWord = Pattern.compile("[\\p{L}-]+|\\n|$");
		this.regexNumber = Pattern.compile("\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|unidentified|true|false)\\b");
		this.regexComment = Pattern.compile("\\/\\*[\\s\\S]*?(?:\\*\\/|$)|\\/\\/.*|#.*");
		this.makeup = new ArrayList<MarkUp>();
		this.lines = new ArrayList<LineData>();
		this.visibleLines = 1;
		(this.text = text).replaceAll("\\r?\\n|\\r", "\n");
	}

	public void addMakeUp(int start, int end, char c, int level) {
		if (!this.removeConflictingMarkUp(start, end, level)) {
			return;
		}
		this.makeup.add(new MarkUp(start, end, c, level));
	}

	public boolean compareMarkUps(MarkUp mu1, MarkUp mu2) {
		return mu1 == null || mu1.start > mu2.start;
	}

	public void formatCodeText() {
		MarkUp markup = null;
		for (int start = 0; (markup = this.getNextMatching(start)) != null; start = markup.end) {
			this.makeup.add(markup);
		}
	}

	public String getFormattedString() {
		StringBuilder builder = new StringBuilder(this.text);
		for (MarkUp entry : this.makeup) {
			builder.insert(entry.start, this.coloricChar + Character.toString(entry.c));
			builder.insert(entry.end, this.coloricChar + Character.toString('r'));
		}
		return builder.toString();
	}

	private MarkUp getNextMatching(int start) {
		MarkUp markup = null;
		String s = this.text.substring(start);
		Matcher matcher = this.regexNumber.matcher(s);
		if (matcher.find()) {
			markup = new MarkUp(matcher.start(), matcher.end(), CustomNpcs.charCodeColor[0].charAt(0), 0);
		}
		matcher = this.regexFunction.matcher(s);
		if (matcher.find()) {
			MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), CustomNpcs.charCodeColor[1].charAt(0), 0);
			if (this.compareMarkUps(markup, markup2)) {
				markup = markup2;
			}
		}
		matcher = this.regexString.matcher(s);
		if (matcher.find()) {
			MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), CustomNpcs.charCodeColor[2].charAt(0), 7);
			if (this.compareMarkUps(markup, markup2)) {
				markup = markup2;
			}
		}
		matcher = this.regexComment.matcher(s);
		if (matcher.find()) {
			MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), CustomNpcs.charCodeColor[3].charAt(0), 7);
			if (this.compareMarkUps(markup, markup2)) {
				markup = markup2;
			}
		}
		if (markup != null) {
			MarkUp markUp = markup;
			markUp.start += start;
			MarkUp markUp2 = markup;
			markUp2.end += start;
		}
		return markup;
	}

	public void init(TrueTypeFont font, int width, int height) {
		this.lineHeight = font.height(this.text);
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
					this.lines.add(new LineData(line.toString(), totalChars, totalChars + line.length(), font.isCode()));
					totalChars += line.length();
					line = new StringBuilder();
				}
				line.append(word);
				i = m.start();
			}
			this.lines.add(new LineData(line.toString(), totalChars, totalChars + line.length() + 1, font.isCode()));
			totalChars += line.length() + 1;
		}
		this.linesCount = this.lines.size();
		this.totalHeight = this.linesCount * this.lineHeight;
		this.visibleLines = Math.max(height / this.lineHeight, 1);
	}

	private boolean removeConflictingMarkUp(int start, int end, int level) {
		List<MarkUp> conflicting = new ArrayList<MarkUp>();
		for (MarkUp m : this.makeup) {
			if ((start >= m.start && start <= m.end) || (end >= m.start && end <= m.end)
					|| (start < m.start && end > m.start)) {
				if (level < m.level || (level == m.level && m.start <= start)) {
					return false;
				}
				conflicting.add(m);
			}
		}
		this.makeup.removeAll(conflicting);
		return true;
	}
}
