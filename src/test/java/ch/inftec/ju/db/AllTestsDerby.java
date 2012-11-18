package ch.inftec.ju.db;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.inftec.ju.db.impl.DbConnectionTest;
import ch.inftec.ju.db.impl.DbRowUtilsTest;
import ch.inftec.ju.db.specific.DerbyTests;
import ch.inftec.ju.util.persistable.DbPersistenceStorageTest;
import ch.inftec.ju.util.persistable.EclipseLinkTest;

@RunWith(Suite.class)
@SuiteClasses({ DbConnectionTest.class, DbRowUtilsTest.class,
		EclipseLinkTest.class, DerbyTests.class,
		DbPersistenceStorageTest.class, })
public class AllTestsDerby {

}
