package ch.inftec.ju.util.libs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CollectionsLibTest.class, IOLibTest.class, JavaTest.class,
		Lang3LibTest.class, YodaTimeTest.class })
public class AllLibTests {
}
