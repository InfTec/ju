package ch.inftec.ju.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RegexTest {
	@Test
	public void toStringTest() {
		assertEquals(new RegexUtil("a*b").toString(), "a*b");
		assertEquals(new RegexUtil("abcdefg").toString(), "abcdefg");
	}
	
	@Test
	public void matches() {
		RegexUtil ru = new RegexUtil("a*b");
		
		assertFalse(ru.isCaseInsensitive());
		
		assertTrue(ru.matches("aaab"));
		assertFalse(ru.matches("aaaabc"));
		
		// Check case insensitive matching
		assertFalse(ru.matches("Aaab"));
		ru.setCaseInsensitive(true);
		assertTrue(ru.matches("Aaab"));
		
		// Check null string matching
		assertFalse(ru.matches(null));
		assertFalse(ru.matches(""));
	}
	
	@Test
	public void containsMatch() {
		RegexUtil ru = new RegexUtil("a*b");
		
		assertTrue(ru.containsMatch("ccabcc"));
		assertTrue(ru.containsMatch("ab"));
		assertFalse(ru.containsMatch("ac"));
		
		// Null string matching
		assertFalse(ru.containsMatch(""));
		assertFalse(ru.containsMatch(null));
	}
	
	@Test
	public void getMatches() {
		RegexUtil ru = new RegexUtil("(a+)(b*)");
		
		RegexUtil.Match[] matches = ru.getMatches("aaabbcacabbb");
		assertEquals(matches.length, 3);
		
		assertEquals(matches[0].getFullMatch(), "aaabb");
		assertArrayEquals(matches[0].getGroups(), new String[] {"aaa", "bb"});
		
		RegexUtil noGroup = new RegexUtil("a*");
		RegexUtil.Match[] noGroupMatches = noGroup.getMatches("aaaabbb");
		assertEquals(noGroupMatches.length, 1);
		assertEquals(noGroupMatches[0].getGroups().length, 0);
		
		RegexUtil oneGroup = new RegexUtil("(a*)");
		RegexUtil.Match[] oneGroupMatches = oneGroup.getMatches("aaaabbb");
		assertEquals(oneGroupMatches.length, 1);
		assertArrayEquals(oneGroupMatches[0].getGroups(), new String[] {"aaaa"});
		
		// Null string matching
		assertEquals(ru.getMatches("").length, 0);
		assertEquals(ru.getMatches(null).length, 0);
		
	}
	
	@Test
	public void getStringMatches() {
		RegexUtil ru = new RegexUtil("a*b");
		assertArrayEquals(ru.getStringMatches("aaabccabcaab"), new String[] {"aaab", "ab", "aab"});
		
		// Theoretically, this pattern would yield an empty result between the two matches, but
		// we are not interested in empty results if there are actual results.
		RegexUtil ruEager = new RegexUtil("a*b*");
		assertArrayEquals(ruEager.getStringMatches("aaabbbcaabb"), new String[] {"aaabbb", "aabb"});
		
		// Empty match. Make sure we get only one of them.
		RegexUtil ruEmpty = new RegexUtil("a*");
		assertArrayEquals(ruEmpty.getStringMatches("bbbbbb"), new String[] {""});
		
		RegexUtil ruSimple = new RegexUtil("a");
		assertArrayEquals(ruSimple.getStringMatches("aacaa"), new String[] {"a", "a", "a", "a"});
		
		// Null string matching
		assertArrayEquals(ru.getStringMatches(""), new String[0]);
		assertArrayEquals(ru.getStringMatches(null), new String[0]);
	}
}
