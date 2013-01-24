package ch.inftec.ju.util.context;

import java.util.List;
import java.util.Map;

/**
 * Interface for an object evaluator, i.e. a class that can evaluate objects using parameters.
 * <p>
 * This interface can be used to evaluate objects in a GenericContext implementation as returned
 * by the GenericContextUtils.builder.
 * 
 * @author Martin
 *
 */
public interface ObjectEvaluator<T> {
	/**
	 * Evaluates objects using the contents of the map as parameters.
	 * @param map Map containing key value pairs. If a single String parameter is to be used,
	 * a Map containing the parameter as both key and value should be submitted.
	 * @return Immutable List of T instances the evaluator yields
	 */
	public List<T> getObjects(Map<String, Object> map);
}
