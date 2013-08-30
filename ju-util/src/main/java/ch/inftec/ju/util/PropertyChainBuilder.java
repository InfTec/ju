package ch.inftec.ju.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create PropertyChain instances.
 * <p>
 * A PropertyChain can evaluate properties using multiple PropertyEvaluators that can
 * be arranged in a chain, priorizing the property evaluation by the order they are added to
 * the chain.
 * 
 * @author Martin
 *
 */
public class PropertyChainBuilder {
	private final List<PropertyEvaluator> evaluators = new ArrayList<>();
	
	// Attributes of the PropertyChain
	private boolean defaultThrowExceptionIfUndefined = false;
	
	/**
	 * Adds an evaluator that evaluates system properties.
	 * @return This builder to allow for chaining
	 */
	public PropertyChainBuilder addSystemPropertyEvaluator() {
		return this.addPropertyEvaluator(new SystemPropertyEvaluator());
	}
	
	/**
	 * Adds a custom implementation of a PropertyEvaluator.
	 * @param evaluator PropertyEvaluator implementation
	 * @return This builder to allow for chaining
	 */
	public PropertyChainBuilder addPropertyEvaluator(PropertyEvaluator evaluator) {
		this.evaluators.add(evaluator);
		return this;
	}
	
	/**
	 * Gets the PropertyChain that was built using this builder.
	 * @return PropertyChain instance
	 */
	public PropertyChain getPropertyChain() {
		return new PropertyChainImpl();
	}
	
	private class PropertyChainImpl implements PropertyChain {

		@Override
		public String get(String key) {
			return this.get(key, defaultThrowExceptionIfUndefined);
		}

		@Override
		public String get(String key, boolean throwExceptionIfNotDefined) {
			Object obj = this.getObject(key, null, throwExceptionIfNotDefined);
			return obj == null ? null : obj.toString();
		}

		@Override
		public String get(String key, String defaultValue) {
			String val = this.get(key, false);
			return val != null ? val : defaultValue;
		}
		
		private Object getObject(String key, Object defaultValue, boolean throwExceptionIfNotDefined) {
			Object obj = this.evaluate(key);
			if (obj == null) {
				if (throwExceptionIfNotDefined) {
					throw new JuRuntimeException("Property undefined: " + key);
				} else {
					return defaultValue;
				}
			}
			return obj;
		}
		
		private Object evaluate(String key) {
			for (PropertyEvaluator evaluator : evaluators) {
				Object val = evaluator.get(key);
				if (val != null) return val;
			}
			return null;
		}
	}
	
	private static class SystemPropertyEvaluator implements PropertyEvaluator {
		public Object get(String key) {
			return System.getProperty(key);
		};
	}
}
