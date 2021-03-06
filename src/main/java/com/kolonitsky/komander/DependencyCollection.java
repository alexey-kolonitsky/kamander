package com.kolonitsky.komander;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Collection of all registered dependencies which used by Komander to resolve
 * '@Injected' metatag. Dependency collection store two kind of dependencies:
 * <ul>
 *     <li>Config fields -- which could be injected by keys</li>
 *     <li>Object -- instances or creator. </li>
 * </ul>
 *
 * To use fields from config one or more configurators must be defined. See
 * addConfigurator() method.
 *
 * Other dependencies resolved by their types.
 *
 * Komander proxy methods of Dependency Collection on it's own API
 */
public class DependencyCollection {

	//--------------------------------------------------------------------------
	// Configuration dependencies
	//--------------------------------------------------------------------------

	private ArrayList<Configurator> _configurators = new ArrayList<>();

	public void addConfigurator(Configurator configurator) {
		if (hasConfigurator(configurator))
			return;

		_configurators.add(configurator);
		String[] properties = configurator.getProperties();
		for (String prop : properties) {
			if (hasDependency(prop))
				continue;
			String value = configurator.get(prop);
			_dependencyByIdMap.put(prop, createDependencyFromInstance(prop, value, configurator, null));
		}
	}

	public boolean hasConfigurator(Configurator configurator) {
		return _configurators.contains(configurator);
	}

	public void removeConfigurator(Configurator configurator) {
		if (!hasConfigurator(configurator))
			return;

		_configurators.remove(configurator);
		for (DependencyDefinition dependency : _dependencyByIdMap.values()) {
			if (dependency.configurator == configurator) {
				_dependencyByIdMap.remove(dependency.key);
			}
		}
	}

	public String getConfigProperty(String key) {
		for (Configurator configurator : _configurators) {
			String value = configurator.get(key);
			if (value != null && !value.isEmpty())
				return value;
		}
		return null;
	}

	public boolean hasConfiguratorProperty(String key) {
		for (Configurator configurator : _configurators) {
			if (configurator.has(key))
				return true;
		}
		return false;
	}

	//--------------------------------------------------------------------------
	// Class dependencies
	//--------------------------------------------------------------------------

	private HashMap<String, DependencyDefinition> _dependencyByIdMap = new HashMap<String, DependencyDefinition>();

	/**
	 * Register dependency instance or creator.
	 *
	 * @param key
	 * @param instance
	 * @param creator
	 */
	public void addInstance(String key, Object instance, IDependencyCreator creator) {
		if (hasDependency(key)) {
			return;
		}
		_dependencyByIdMap.put(key, createDependencyFromInstance(key, instance, null, creator));
	}

	public DependencyDefinition createDependencyFromInstance(String key, Object instance, Configurator configurator, IDependencyCreator creator) {
		DependencyDefinition result = new DependencyDefinition();
		result.key = key;
		result.instance = instance;
		result.configurator = configurator;
		result.creator = creator;
		return result;
	}

	/**
	 * Check if dependency already registered.
	 * @param key
	 * @return
	 */
	public boolean hasDependency(String key) {
		return _dependencyByIdMap.containsKey(key);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public DependencyDefinition getDependency(String key) {
		if (hasDependency(key)) {
			return _dependencyByIdMap.get(key);
		}
		return null;
	}

}
