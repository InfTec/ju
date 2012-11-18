package ch.inftec.ju.util.persistable;

import java.util.List;

import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.XString;
import ch.inftec.ju.util.persistable.GenericMemento.MementoAttribute;

/**
 * Test implementation of MementoStorage that writes the Memento to a String.
 * <p>
 * Can be useful to test Memento creation. Use toString to get the generated String.
 * <p>
 * Use the GenericMementoUtils.newStringMementoStorage method to get an instance of this storage.
 * @author Martin
 *
 */
final class StringMementoStorage implements MementoStorage {
	public long id = 0;
	public XString xs = new XString();
		
	@Override
	public Long persistMemento(GenericMemento memento, String type) {
		long id = this.id++;
		xs.addLine("Object: " + id);
		
		xs.increaseIndent();
		
		for (MementoAttribute attribute : memento.getAttributes()) {
			xs.addLine("Attribute: ", attribute.getKey(), "=");			
			if (attribute.getStringValue() != null) xs.addText(attribute.getStringValue());
			else if (attribute.getLongValue() != null) xs.addText("L:", attribute.getLongValue());
			else if (attribute.getDateValue() != null) xs.addText("D:", JuStringUtils.DATE_FORMAT_SECONDS.format(attribute.getDateValue()));
			
		}
		
		for (GenericMemento child : memento.getChildren()) {
			this.persistMemento(child, null);
		}
		
		xs.decreaseIndent();

		return id;
	}

	@Override
	public GenericMementoItem loadMemento(Long id) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	@Override
	public String toString() {
		return this.xs.toString();
	}

	@Override
	public List<GenericMementoItem> loadMementos(int maxCount) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
