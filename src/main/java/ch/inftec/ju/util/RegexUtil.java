package ch.inftec.ju.util;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class containing regular expression related methods.
 * By default, the RegexUtil matches case sensitive.
 * @author tgdmemae
 *
 */
public final class RegexUtil {
	public static final String WHITESPACE = "\\s";
	public static final String DIGIT = "\\d";
	public static final String WORD = "\\w";
	
	private transient Pattern pattern;
	private final String patternString;
	
	private boolean isCaseInsensitive = false;
	
	public RegexUtil(String pattern) {
		this.patternString = pattern;
	}
	
	/**
	 * Gets whether the pattern ignores case when matching.
	 * @return True if matching is case insensitive
	 */
	public boolean isCaseInsensitive() {
		return this.isCaseInsensitive;
	}
	
	/**
	 * Sets whether the pattern ignores case or not.
	 * @param isCaseInsensitive True if the pattern should ignore case, false otherwise
	 */
	public void setCaseInsensitive(boolean isCaseInsensitive) {
		if (this.isCaseInsensitive != isCaseInsensitive) {
			this.isCaseInsensitive = isCaseInsensitive;
			this.invalidatePattern();
		}
	}
	
	/**
	 * Invalidates the pattern, i.e. re-compiles the pattern the next time it
	 * is needed.
	 */
	private void invalidatePattern() {
		this.pattern = null;
	}
	
	/**
	 * Gets the compiled pattern of this RegexUtil instance.
	 * @return Pattern instance
	 */
	private Pattern getPattern() {
		if (this.pattern == null) {
			int flags = this.isCaseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
			
			this.pattern = Pattern.compile(this.patternString, flags);
		}
		
		return this.pattern;
	}
	
	/**
	 * Checks if a String matches the pattern.
	 * @param s String
	 * @return True if the string matches the pattern
	 */
	public boolean matches(String s) {
		return s != null && this.getPattern().matcher(s).matches();
	}
	
	/**
	 * Checks if the String contains a Substring that matches the pattern.
	 * @param s String
	 * @return True if the string contains a substring that matches the pattern
	 */
	public boolean containsMatch(String s) {
		return s != null && this.getPattern().matcher(s).find();
	}
	
	/**
	 * Gets all matches of the pattern in the String, containing information about
	 * groups of the match as well.
	 * @param s String
	 * @return Array of Match instances containing match and group information
	 */
	public Match[] getMatches(String s) {
		if (s == null) {
			return new Match[0];
		} else {
			Matcher matcher = this.getPattern().matcher(s);
			int index = 0;
			ArrayList<Match> matches = new ArrayList<Match>();
			
			Match firstEmptyMatch = null;
			
			while (index < s.length() && matcher.find(index)) {
				MatchResult result = matcher.toMatchResult();
				
				// Only keep the first empty result in case we don't find anything else...
				if (index == matcher.end()) {
					if (firstEmptyMatch == null) firstEmptyMatch = new Match(result);
					
					index = matcher.end() + 1;
				} else {
					matches.add(new Match(matcher.toMatchResult()));
					index = matcher.end();
				}
			}
			
			if (matches.size() == 0 && firstEmptyMatch != null) {
				matches.add(firstEmptyMatch);
			}
			
			return (Match[])matches.toArray(new Match[0]);
		}
	}
	
	/**
	 * Gets all matches of the pattern in the String. Only returns the main matches,
	 * not containing any detailed group information. If you need those, use getMatches
	 * instead.
	 * @param s String
	 * @return Array of matches in the string
	 */
	public String[] getStringMatches(String s) {
		Match matches[] = this.getMatches(s);
		
		String stringMatches[] = new String[matches.length];
		for (int i = 0; i < stringMatches.length; i++) {
			stringMatches[i] = matches[i].getFullMatch();
		}
		
		return stringMatches;
	}
	
	@Override
	public String toString() {
		return this.patternString;
	}
	
	/**
	 * Result object for Regex matches, containing information about groups as well.
	 * @author tgdmemae
	 *
	 */
	public class Match {
		private MatchResult matchResult;
		
		/**
		 * Creates a new Match instance around the specified MatchResult
		 * @param matchResult
		 */
		private Match(MatchResult matchResult) {
			this.matchResult = matchResult;
		}
		
		/**
		 * Gets the full match string (i.e. zero-th group)
		 * @return Full match
		 */
		public String getFullMatch() {
			return this.matchResult.group();
		}
		
		/**
		 * Gets an array of all groups of the match (if any). Note that this does not
		 * contain the full match or implicit outer group.
		 * @return Groups of the match
		 */
		public String[] getGroups() {
			ArrayList<String> groups = new ArrayList<String>();
			for (int i = 0; i < this.matchResult.groupCount(); i++) {
				groups.add(this.matchResult.group(i + 1));
			}
			
			return (String[])groups.toArray(new String[0]);
		}
	}	
}
