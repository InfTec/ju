package ch.inftec.ju.util.comparison;

/**
 * Interface for classes that perform equality tests on two instances.
 * 
 * The equality tested by such a class can yield different results than
 * the object's equals method. For instance, the tester might perform
 * some conversions prior to testing, like converting an Integer to
 * a Long.
 * 
 * @author tgdmemae
 *
 * @param <T> Common supertype of the objects to test.
 */
public interface EqualityTester<T> {
	/**
	 * Tests it the two objects are equal.
	 * @param obj1 Object 1
	 * @param obj2 Object 2
	 * @return True if the objects are equal
	 */
	public boolean equals(T obj1, T obj2);
}
