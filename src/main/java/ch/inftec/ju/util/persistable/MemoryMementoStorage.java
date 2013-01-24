package ch.inftec.ju.util.persistable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of the MementoStorage interface holding data in memory.
 * @author Martin
 *
 */
final class MemoryMementoStorage implements MementoStorage {
	private TreeMap<Long, GenericMemento> mementos = new TreeMap<>();
	private Map<Long, String> mementoTypes = new HashMap<>();
	
	@Override
	public Long persistMemento(GenericMemento memento, String type) {
		long id = this.mementos.size() == 0 ? 0 : this.mementos.lastKey() + 1;
		
		// Persist memento
		this.mementos.put(id, memento);
		if (type != null) this.mementoTypes.put(id, type);
		
		// Persist children
		for (GenericMemento childMemento : memento.getChildren()) {
			this.persistMemento(childMemento, null);
		}
		
		return id;
	}

	@Override
	public GenericMementoItem loadMemento(Long id) {
		if (id == null) return null;

		GenericMemento memento = this.mementos.get(id);
		if (memento == null) return null;

		return GenericMementoUtils.newGenericMementoItem(this.mementos.get(id), id, this.mementoTypes.get(id));
	}
	
	@Override
	public List<GenericMementoItem> loadMementos(int maxCount) {
		List<GenericMementoItem> list = new ArrayList<>();
		
		Iterator<Long> i = this.mementos.descendingKeySet().iterator();
		while (list.size() < maxCount && i.hasNext()) {
			Long key = i.next();
			list.add(this.loadMemento(key));
		}
		
		return list;
	}
}
