package ch.inftec.ju.util.general;

import ch.inftec.ju.util.event.EventNotifier;
import ch.inftec.ju.util.event.UpdateListener;

/**
 * A descriptor provides methods to retrieve information to display an object.
 * <p>
 * Each descriptor consist of at least a name. The value of the name must
 * never change and thus must be known the first time the name property is read. It should be
 * a very concise information about the object that may contain details like an ID that
 * must be available as soon as the name property is accessed by a consumer.
 * <p>
 * In its simplest form, the description can equal the name. However, it may also change as the 
 * underlying object changes. In this case, the Descriptor has to notify any listeners
 * with an appropriate change event.
 * <p>
 * All descriptive objects returned by getObject are also subject to changes (and appropriate
 * change events).
 * <p>
 * This means that all consumers of the Descriptor must either be able to handle those changes
 * or rely on the name only.
 * <p>
 * The toString method of a descriptor should return its name.
 * 
 * @author Martin
 *
 */
public interface Descriptor {
	/**
	 * Gets the immutable name of the object. A name should be as concise as possible, i.e. a single
	 * word with an optional ID.
	 * @return Name
	 */
	public String getName();
	
	/**
	 * Gets a description that may change. This is usually a bit longer than the name and 
	 * contains more information. Also, the description should be more human friendly.
	 * @return Description
	 */
	public String getDescription();
	
	/**
	 * Gets any additional object describing this object. This may be a size information
	 * or an icon or any other object.
	 * @param clazz Class type of the descriptive object or null if no object exists
	 * for the specified type.
	 */
	public <T> T getObject(Class<T> clazz);
	
	/**
	 * Gets an EventNotifier instance that can be used to register listeners for
	 * update events.
	 * <p>
	 * Update events are fired whenever the description of the object related
	 * to the Descriptor have changed.
	 * @return EventNotifier instance
	 */
	public EventNotifier<UpdateListener<Descriptor>> getUpdateNotifier();
}
