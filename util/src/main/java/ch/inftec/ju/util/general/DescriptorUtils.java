package ch.inftec.ju.util.general;

import ch.inftec.ju.util.context.GenericContext;
import ch.inftec.ju.util.context.GenericContextUtils;
import ch.inftec.ju.util.context.GenericContextUtils.GenericContextBuilder;
import ch.inftec.ju.util.event.EventNotifier;
import ch.inftec.ju.util.event.JuEventObject;
import ch.inftec.ju.util.event.JuEventUtils;
import ch.inftec.ju.util.event.JuEventUtils.UpdateEventNotifier;
import ch.inftec.ju.util.event.UpdateListener;

/**
 * DescriptorUtils contains utility methods related to the Descriptor interface.
 * 
 * The class cannot be instantiated nor extended.
 * @author tgdmemae
 *
 */
public final class DescriptorUtils {
	/**
	 * Don't instantiate.
	 */
	private DescriptorUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Creates a new immutable Descriptor instance with the specified name.
	 * <p>
	 * The description is also set to the name.
	 * @param name Name
	 * @return New Descriptor instance
	 */
	public static Descriptor newInstance(String name) {
		return new DescriptorImpl(name, name);
	}
	
	/**
	 * Creates a new immutable Descriptor instance with the specified name.
	 * @param name Name
	 * @param description Description
	 * @return New Descriptor instance
	 */
	public static Descriptor newInstance(String name, String description) {
		return new DescriptorImpl(name, description);
	}
	
	/**
	 * Gets a DescriptorBuilder that allows to add any number of class/object
	 * pairs to the Descriptor.
	 * <p>
	 * The description default to the name, but may be overwritten with 
	 * the builder's description method.
	 * @param name Name and initial description
	 * @return New Descriptor instance
	 */
	public static DescriptorBuilder builder(String name) {
		return new DescriptorBuilder(name, name);
	}
	
	public static interface DescriptionEvaluator {
		public String getDescription();
		public <T> T getObject(Class<T> clazz);
		public EventNotifier<UpdateListener<DescriptionEvaluator>> getUpdateNotifier();
	}
	
	public static abstract class AbstractDescriptionEvaluator implements DescriptionEvaluator {
		private final UpdateEventNotifier<DescriptionEvaluator> updateNotifier = JuEventUtils.newUpdateEventNotifier();
		private String description;
		
		@Override
		public final String getDescription() {
			if (this.description == null) {
				this.description = this.evaluateDescription();
			}
			
			return this.description;
		}
		
		/**
		 * Extending classes can override this method to evaluate a description.
		 * <p>
		 * The default implementation just returns null, i.e. the Descriptor will use the initial
		 * description.
		 * @return Current description
		 */
		protected String evaluateDescription() {
			return null;
		}

		@Override
		public final <T> T getObject(Class<T> clazz) {
			return this.evaluateObject(clazz);
		}
		
		/**
		 * Extending classes can override this method to evaluate an object for a specific type.
		 * <p>
		 * The default implementation just returns null, i.e. the Descriptor will use the statically
		 * set object (if any).
		 * @param clazz Class of type
		 * @return Object for the specified type
		 */
		protected <T> T evaluateObject(Class<T> clazz) {
			return null;
		}

		/**
		 * Extending classes must call this method whenever the description has changed.
		 * <p>
		 * This will result in a call to evaluateDescription the next time the
		 * getDescription method is called.
		 */
		protected final void updateDescription() {
			this.description = null;
			this.updateNotifier.fireUpdateEvent(this);
		}
		
		@Override
		public EventNotifier<UpdateListener<DescriptionEvaluator>> getUpdateNotifier() {
			return this.updateNotifier;
		}
		
	}
	
	/**
	 * Default implementation of the UpdatableDescriptor interface.
	 * @author Martin
	 *
	 */
	private static final class DescriptorImpl implements Descriptor, UpdateListener<DescriptionEvaluator> {
		final String name;
		String description;
		private DescriptionEvaluator descriptionEvaluator;
		
		/**
		 * GenericContext used for the object lookup and storing.
		 */
		GenericContext context;
		
		private UpdateEventNotifier<Descriptor> updateNotifier = JuEventUtils.newUpdateEventNotifier();
		
		/**
		 * Creates a new Descriptor with the specified name.
		 * @param name Name
		 * @param description Initial description
		 */
		private DescriptorImpl(String name, String description) {
			this.name = name;
			this.description = description;
		}
		
		@Override
		public String getName() {
			return this.name;
		}
		
		@Override
		public String getDescription() {
			if (this.descriptionEvaluator != null && this.descriptionEvaluator.getDescription() != null) {
				return this.descriptionEvaluator.getDescription();
			} else {
				return this.description;
			}
		}
		
		void setDescriptionEvaluator(DescriptionEvaluator descriptionEvaluator) {
			if (this.descriptionEvaluator != null) {
				this.descriptionEvaluator.getUpdateNotifier().removeListener(this);
			}
			
			this.descriptionEvaluator = descriptionEvaluator;
			if (this.descriptionEvaluator != null) {
				this.descriptionEvaluator.getUpdateNotifier().addListener(this);
			}			
		}
		
		void setGenericContext(GenericContext context) {
			this.context = context;
		}
		
		@Override
		public <T> T getObject(Class<T> clazz) {
			if (this.descriptionEvaluator != null && this.descriptionEvaluator.getObject(clazz) != null) {
				return this.descriptionEvaluator.getObject(clazz);
			} else {
				return GenericContextUtils.asX(this.context).getObject(clazz);
			}
		}
		
		@Override
		public EventNotifier<UpdateListener<Descriptor>> getUpdateNotifier() {
			return this.updateNotifier;
		}
		
		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public void updated(JuEventObject<DescriptionEvaluator> event) {
			this.updateNotifier.fireUpdateEvent(this);			
		}
	}
	
	/**
	 * Builder to construct Descriptor instance. Use the DescriptorUtils.builder() method
	 * to use the builder.
	 * @author tgdmemae
	 *
	 */
	public static final class DescriptorBuilder {
		private DescriptorImpl descriptorImpl;
		GenericContextBuilder contextBuilder = GenericContextUtils.builder();
		
		private DescriptorBuilder(String name, String description) {
			this.descriptorImpl = new DescriptorImpl(name, description);
		}
		
		/**
		 * Sets the initial description of the Descriptor.
		 * @param description Description
		 * @return This builder to allow for chaining
		 */
		public DescriptorBuilder description(String description) {
			this.descriptorImpl.description = description;
			return this;
		}
		
		/**
		 * Sets a DescriptionEvaluator to dynamically calculate the description and the
		 * objects of the Descriptor.
		 * <p>
		 * DescriptorUtils.AbstractDescriptionEvaluator provides a base class for evaluators.
		 * @param descriptionEvaluator DescriptionEvaluator instance
		 * @return This builder to allow for chaining
		 */
		public DescriptorBuilder evaluator(DescriptionEvaluator descriptionEvaluator) {
			this.descriptorImpl.setDescriptionEvaluator(descriptionEvaluator);
			return this;
		}
		
		/**
		 * Sets the object for the specified type. If the mapping for the
		 * type already exists, it is overridden.
		 * @param clazz Class type
		 * @param obj Object to be associated with the class
		 */
		public <T> DescriptorBuilder setObject(Class<T> clazz, T obj) {
			this.contextBuilder.setObject(clazz, obj);
			return this;
		}
		
		/**
		 * Gets the Descriptor instance built with this builder. Note that this 
		 * method always returns the same instance.
		 * @return Descriptor
		 */
		public Descriptor getDescriptor() {
			this.descriptorImpl.setGenericContext(this.contextBuilder.build());
			return this.descriptorImpl;
		}
	}
	
//	/**
//	 * Wrapper around a descriptor instance. Can be used to override 
//	 * specific elements of the descriptor.
//	 * 
//	 * TODO: Still needed? Refactor...
//	 * @author Martin
//	 *
//	 */
//	private class DescriptorDecorator implements Descriptor {
//		private Descriptor descriptor;
//		
//		private String namePrefix;
//		private String descriptionPrefix;
//		
//		/**
//		 * Creates a new decorator for the specified descriptor.
//		 * @param descriptor Base descriptor to decorate
//		 */
//		public DescriptorDecorator(Descriptor descriptor) {
//			this.descriptor = descriptor;
//		}
//		
//		public void setPrefix(String namePrefix, String descriptionPrefix) {
//			this.namePrefix = namePrefix;
//			this.descriptionPrefix = descriptionPrefix;
//		}
//		
//		@Override
//		public String getName() {
//			String name = this.descriptor.getName();
//			
//			return StringUtils.isEmpty(this.namePrefix) ? name : this.namePrefix + name;
//		}
//
//		@Override
//		public String getDescription() {
//			String description = this.descriptor.getDescription();
//			
//			return StringUtils.isEmpty(this.descriptionPrefix) ? description : this.descriptionPrefix + description;
//		}
//
//		@Override
//		public <T> T getObject(Class<T> clazz) {
//			return this.descriptor.getObject(clazz);
//		}
//
//	}
}