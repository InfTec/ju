package ch.inftec.ju.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.inftec.ju.util.comparison.ComparisonTest;
import ch.inftec.ju.util.context.GenericContextTest;
import ch.inftec.ju.util.general.DescriptorTest;
import ch.inftec.ju.util.io.NewLineReaderTest;
import ch.inftec.ju.util.libs.AllLibTests;
import ch.inftec.ju.util.persistable.AllPersistableTests;
import ch.inftec.ju.util.xml.XmlTest;

@RunWith(Suite.class)
@SuiteClasses({ AllUtilTests.class, ComparisonTest.class,
		GenericContextTest.class, DescriptorTest.class,
		NewLineReaderTest.class, AllLibTests.class, AllPersistableTests.class, XmlTest.class })
public class AllTests {

}
