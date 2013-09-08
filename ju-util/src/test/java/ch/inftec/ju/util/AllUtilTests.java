package ch.inftec.ju.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AuthUtilTest.class, CollectionTest.class, ConversionUtilTest.class, CsvTableLookupTest.class, 
	    IOTest.class, JuBeanUtilsTest.class, LoggingTest.class, ObjectUtilTest.class, PropertyChainTest.class,
		ReflectTest.class, RegexTest.class, StringTest.class, TimerTest.class,
		XStringTest.class })
public class AllUtilTests {
}
