package ch.inftec.ju.util.persistable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;

import ch.inftec.ju.util.comparison.EqualityTester;
import ch.inftec.ju.util.persistable.GenericMementoUtils.GenericMementoBuilder;
import ch.inftec.ju.util.persistable.GenericMementoUtils.TypeHandlerBuilder;

/**
 * Test class for GenericMemento related classes.
 * @author Martin
 *
 */
public class GenericMementoTest {
	@Test
	public void genericMementoUsage() {
		ItemEqualityTester equalityTester = new ItemEqualityTester();
		
		Date now = new Date();
		Date later = new Date(now.getTime() + 1000);
		
		PersistableItem i1 = new PersistableItem(1L, "one", now);
		i1.addChild(new PersistableItem(2L, "two", later));
		
		GenericMemento memento = i1.createMemento();
		
		PersistableItem l1 = new PersistableItem();
		l1.setMemento(memento);
		
		Assert.assertTrue(equalityTester.equals(i1, l1));
	}
	
	private static class ItemEqualityTester implements EqualityTester<PersistableItem> {
		@Override
		public boolean equals(PersistableItem i1, PersistableItem i2) {
			if (i1 == i2) return true;
			else if (i1 == null || i2 == null) return false;
			
			if (ObjectUtils.equals(i1.stringVal, i2.stringVal)
					&& ObjectUtils.equals(i1.longVal, i2.longVal)
					&& ObjectUtils.equals(i1.dateVal, i2.dateVal)
					&& i1.children.size() == i2.children.size()) {
				
				for (int i = 0; i < i1.children.size(); i++) {
					if (!this.equals(i1.children.get(i), i2.children.get(i))) return false;
				}
				
				return true;
			} else {
				return false;
			}
		}		
	}
	
	private static class PersistableItem implements Persistable {
		long longVal;
		String stringVal;
		Date dateVal;
		
		List<PersistableItem> children = new ArrayList<>();
		
		PersistableItem() {			
		}
		
		PersistableItem(long longVal, String stringVal, Date dateVal) {
			this.longVal = longVal;
			this.stringVal = stringVal;
			this.dateVal = dateVal;
		}
		
		void addChild(PersistableItem item) {
			children.add(item);
		}

		@Override
		public GenericMemento createMemento() {			
			GenericMementoBuilder builder = GenericMementoUtils.builder();
			this.buildMemento(builder);
			return builder.build();
		}
		
		private void buildMemento(GenericMementoBuilder builder) {
			builder
				.add("stringVal", this.stringVal)
				.add("longVal", this.longVal)
				.add("dateVal", this.dateVal);
			
			for (PersistableItem child : this.children) {
				GenericMementoBuilder childBuilder = builder.newChild();
				child.buildMemento(childBuilder);
				childBuilder.childDone();
			}
		}

		@Override
		public void setMemento(GenericMemento memento) {
			GenericMementoX m = GenericMementoUtils.asX(memento);
			
			this.children.clear();
			this.stringVal = m.getStringValue("stringVal");
			this.longVal = m.getLongValue("longVal");
			this.dateVal = m.getDateValue("dateVal");
			
			for (GenericMemento childMemento : m.getChildren()) {
				PersistableItem child = new PersistableItem();
				child.setMemento(childMemento);
				this.children.add(child);
			}
		}
	}
	
	@Test
	public void genericMementoX() {
		GenericMemento genericMemento = GenericMementoUtils.builder()
			.add("key1", "val1")
			.add("key2", "val2")
			.build();
				
		GenericMementoX g = GenericMementoUtils.asX(genericMemento);
		Assert.assertEquals("val1", g.getAttribute("key1").getStringValue());
	}
	
	@Test
	public void testNull() {
		// Null if it isn't defined
		GenericMementoX g = GenericMementoUtils.asX(GenericMementoUtils.builder().build());
		Assert.assertEquals(null, g.getAttribute(null));

		// Null key and null values
		GenericMemento genericMementoNull = GenericMementoUtils.builder()
				.add(null, "nullVal")
				.add("nullKey", null)
				.build();
		
		GenericMementoX gNull = GenericMementoUtils.asX(genericMementoNull);
		Assert.assertEquals("nullVal", gNull.getAttribute(null).getStringValue());
		Assert.assertNull(gNull.getStringValue("nullKey"));
		Assert.assertNull(gNull.getLongValue("nullKey"));
		Assert.assertNull(gNull.getDateValue("nullKey"));
	}
	
	/**
	 * Test the building of a type handler.
	 */
	@Test
	public void typeHandler() {
		TypeHandlerBuilder builder = GenericMementoUtils.newTypeHandler();
		
		TypeHandler1 th1 = new TypeHandler1();
		TypeHandler2 th2 = new TypeHandler2();
		
		// Check default type names
		Assert.assertEquals(TypeHandler1.class.getName(), builder.getHandler().getTypeName(th1));
		Assert.assertEquals(TypeHandler2.class.getName(), builder.getHandler().getTypeName(th2));
		
		// Check default instantiation
		Assert.assertEquals(TypeHandler1.class, builder.getHandler().newInstance(TypeHandler1.class.getName()).getClass());
		
		// Check automatic mapping without field
		builder.addMapping(TypeHandler1.class);
		Assert.assertEquals(TypeHandler1.class.getName(), builder.getHandler().getTypeName(th1));
		
		// Check automatic mapping with field
		builder.addMapping(TypeHandler2.class);
		Assert.assertEquals("H2", builder.getHandler().getTypeName(th2));
		Assert.assertEquals(TypeHandler2.class, builder.getHandler().newInstance("H2").getClass());
		
		// Create instance of private class
		Assert.assertEquals(TypeHandler3.class, builder.getHandler().newInstance(TypeHandler3.class.getName()).getClass());
	}
	
	@Test
	public void dynamicTypeHandler() {
		TypeHandler handler = GenericMementoUtils.newTypeHandler().dynamic(true).getHandler();
		
		Assert.assertEquals(TypeHandler1.class.getName(), handler.getTypeName(new TypeHandler1()));
		Assert.assertEquals("H2", handler.getTypeName(new TypeHandler2()));
		Assert.assertEquals(TypeHandler2.class, handler.newInstance("H2").getClass());
	}
	
	public static class TypeHandler1 implements Persistable {
		@Override
		public GenericMemento createMemento() {
			return null;
		}

		@Override
		public void setMemento(GenericMemento memento) {
		}
	}
	
	static final class TypeHandler2 extends TypeHandler1 {
		@SuppressWarnings("unused")
		private static String MEMENTO_TYPE_NAME = "H2";
	}
	
	private static final class TypeHandler3 extends TypeHandler1 {		
	}
}

