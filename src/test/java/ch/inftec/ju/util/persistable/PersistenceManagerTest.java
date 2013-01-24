package ch.inftec.ju.util.persistable;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.util.TestUtils;
import ch.inftec.ju.util.persistable.GenericMemento.MementoAttribute;
import ch.inftec.ju.util.persistable.GenericMementoUtils.GenericMementoBuilder;

public class PersistenceManagerTest {
	@Test
	public void stringStorage() {
		StringMementoStorage storage = new StringMementoStorage();
		
		PersistableManager manager = GenericMementoUtils.newPersistableManager(storage, GenericMementoUtils.DEFAULT_TYPE_HANDLER);
		
		manager.persist(this.getTestProperties());
		
		TestUtils.assertEqualsResource("stringStorage.txt", storage.toString());
	}
	
	@Test
	public void defaultTypeHandler() {
		TypeHandler h = GenericMementoUtils.DEFAULT_TYPE_HANDLER;
		
		String className = PersistableProperties.class.getName();
		Assert.assertEquals("ch.inftec.ju.util.persistable.PersistenceManagerTest$PersistableProperties", className);
		
		Assert.assertEquals(className, h.getTypeName(new PersistableProperties()));
		Assert.assertEquals(PersistableProperties.class, h.newInstance(className).getClass());
		
		
	}
	
	@Test
	public void memoryStorage() {
		MemoryMementoStorage storage = new MemoryMementoStorage();
		
		PersistableManager manager = GenericMementoUtils.newPersistableManager(storage, GenericMementoUtils.DEFAULT_TYPE_HANDLER);
		
		Long id = manager.persist(this.getTestProperties());
		
		GenericMemento o1 = storage.loadMemento(id).getMemento();
		
		Assert.assertEquals(1, o1.getChildren().size());
		Assert.assertEquals(3, o1.getAttributes().size());
		Assert.assertEquals("p1", o1.getAttributes().get(0).getKey());
		Assert.assertEquals("v1", o1.getAttributes().get(0).getStringValue());
		
		GenericMemento o2 = storage.loadMemento(1L).getMemento();
		Assert.assertEquals("v23", o2.getAttributes().get(2).getStringValue());
		
		// Null
		Assert.assertNull(storage.loadMemento(99L));
		
		// MementoAttribute types
		Date dateNow = new Date();
		
		GenericMemento m = GenericMementoUtils.builder()
				.add("k1", "string")
				.add("k2", dateNow)
				.add("k3", 99L)
				.add("k4", "hello", dateNow, 100L)
				.build();
		
		Long mId = storage.persistMemento(m, "M");
		GenericMemento ml = storage.loadMemento(mId).getMemento();
		GenericMementoX mlx = new GenericMementoX(ml);
		Assert.assertEquals(4, ml.getAttributes().size());
		Assert.assertEquals("string", mlx.getAttribute("k1").getStringValue());
		Assert.assertEquals(dateNow, mlx.getAttribute("k2").getDateValue());
		Assert.assertEquals(new Long(99), mlx.getAttribute("k3").getLongValue());
		Assert.assertEquals("hello", mlx.getAttribute("k4").getStringValue());
	}
	
	@Test
	public void persistenceManagerBase() {
		MemoryMementoStorage storage = new MemoryMementoStorage();
		
		PersistableManager manager = GenericMementoUtils.newPersistableManager(storage, GenericMementoUtils.DEFAULT_TYPE_HANDLER);
		
		manager.persist(this.getTestProperties());
		
		PersistableProperties p1 = (PersistableProperties)manager.load(0L);
		
		Assert.assertEquals(3, p1.size());
		Assert.assertEquals("v1", p1.get("p1"));
		Assert.assertEquals("v2", p1.get("p2"));
		Assert.assertEquals("v3", p1.get("p3"));
		
		Assert.assertEquals(1, p1.children.size());		
		
		// Null
		Assert.assertNull(manager.load(99L));
	}
	
	private PersistableProperties getTestProperties() {
		PersistableProperties pp = new PersistableProperties();
		pp.put("p1", "v1");
		pp.put("p2", "v2");
		pp.put("p3", "v3");
		
		PersistableProperties pp2 = new PersistableProperties();
		pp2.put("p21", "v21");
		pp2.put("p22", "v22");
		pp2.put("p23", "v23");
		
		PersistableProperties pp3 = new PersistableProperties();
		pp3.put("p31", "v31");
		pp3.put("p32", "v32");
		pp3.put("p33", "v33");
		
		pp.addChild(pp2);
		
		pp2.addChild(pp3);
		pp2.addChild(pp3);
		
		return pp;
	}
	
	public static class PersistableProperties extends ListOrderedMap<String, String> implements Persistable {
		private ArrayList<PersistableProperties> children = new ArrayList<>();

		@Override
		public GenericMemento createMemento() {
			return this.createMemento(GenericMementoUtils.builder()).build();
		}
		
		private GenericMementoBuilder createMemento(GenericMementoBuilder builder) {
			for (String name : this.keySet()) {
				String val = this.get(name).toString();
				
				builder.add(name, val);
			}
			
			for (PersistableProperties child : this.children) {
				GenericMementoBuilder childBuilder = builder.newChild();
				child.createMemento(childBuilder);
				childBuilder.childDone();
			}
			
			return builder;
		}

		@Override
		public void setMemento(GenericMemento memento) {
			this.clear();
			this.children.clear();
			
			for (MementoAttribute attr : memento.getAttributes()) {
				this.put(attr.getKey(), attr.getStringValue());
			}
			
			for (GenericMemento child : memento.getChildren()) {
				PersistableProperties pChild = new PersistableProperties();
				pChild.setMemento(child);
				this.addChild(pChild);
			}
		}
		
		
		public void addChild(PersistableProperties child) {
			this.children.add(child);
		}
	}
}
