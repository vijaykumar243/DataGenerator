package de.frosner.datagenerator.generator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import net.sf.qualitycheck.Check;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.frosner.datagenerator.distributions.VariableParameter;
import de.frosner.datagenerator.exceptions.CircularDependencyException;
import de.frosner.datagenerator.features.FeatureDefinition;
import de.frosner.datagenerator.util.VisibleForTesting;

public class FeatureDefinitionGraph implements Iterable<FeatureDefinition> {

	@VisibleForTesting
	final Map<FeatureDefinition, Set<FeatureDefinitionParameterPair>> _adjacentNodes = Maps.newHashMap();
	@VisibleForTesting
	final LinkedList<FeatureDefinition> _topologicalOrder = Lists.newLinkedList();

	public static FeatureDefinitionGraph createCopyOf(FeatureDefinitionGraph graph) {
		FeatureDefinitionGraph copy = new FeatureDefinitionGraph();
		for (FeatureDefinition featureDefinition : graph._adjacentNodes.keySet()) {
			Set<FeatureDefinitionParameterPair> dependencies = Sets.newHashSet();
			dependencies.addAll(graph._adjacentNodes.get(featureDefinition));
			copy._adjacentNodes.put(featureDefinition, dependencies);
		}
		copy._topologicalOrder.addAll(graph._topologicalOrder);
		return copy;
	}

	public static FeatureDefinitionGraph createFromList(List<FeatureDefinition> featureDefinitions) {
		FeatureDefinitionGraph graph = new FeatureDefinitionGraph();
		// TODO create graph.addFeatureDefinitions(featureDefinitions)
		for (FeatureDefinition featureDefinition : featureDefinitions) {
			graph.addFeatureDefinition(featureDefinition);
		}
		for (FeatureDefinition featureDefinition : featureDefinitions) {
			for (VariableParameter<?> parameter : featureDefinition.getDependentParameters()) {
				graph.addFeatureDefinitionParameterDependency(parameter.getFeatureDefinitionConditionedOn(), featureDefinition,
						parameter);
			}
		}

		return graph;
	}

	public boolean addFeatureDefinition(FeatureDefinition featureDefinition) {
		Check.notNull(featureDefinition, "featureDefinition");
		if (_adjacentNodes.containsKey(featureDefinition)) {
			return false;
		}
		_adjacentNodes.put(featureDefinition, new HashSet<FeatureDefinitionParameterPair>());
		_topologicalOrder.add(featureDefinition);
		return true;
	}

	private void removeFeatureDefinition(FeatureDefinition featureDefinition) {
		_adjacentNodes.remove(featureDefinition);
		_topologicalOrder.remove(featureDefinition);
	}

	public boolean addFeatureDefinitionParameterDependency(@Nonnull FeatureDefinition parentFeature,
			@Nonnull FeatureDefinition childFeature, @Nonnull VariableParameter<?> childParameter) {
		Check.notNull(parentFeature, "parentFeature");
		Check.notNull(childFeature, "childFeature");
		Check.notNull(childParameter, "childParameter");
		Check.stateIsTrue(_adjacentNodes.containsKey(parentFeature), "parent feature must be part of the graph");

		Set<FeatureDefinitionParameterPair> children = _adjacentNodes.get(parentFeature);
		FeatureDefinitionParameterPair pair = new FeatureDefinitionParameterPair(childFeature, childParameter);
		if (children.add(pair)) {
			addFeatureDefinition(childFeature);
			if (!containsPath(childFeature, parentFeature)) {
				adjustInsertionOrder(parentFeature, childFeature);
				return true;
			} else {
				children.remove(pair);
				removeFeatureDefinition(childFeature);
				throw new CircularDependencyException();
			}
		} else {
			return false;
		}
	}

	public boolean isEmpty() {
		return _adjacentNodes.keySet().isEmpty();
	}

	public boolean isLeaf(@Nonnull FeatureDefinition definition) {
		Check.notNull(definition);
		if (!_adjacentNodes.containsKey(definition)) {
			return false;
		}
		return _adjacentNodes.get(definition).isEmpty();
	}

	public List<VariableParameter<?>> getDependentParameters(@Nonnull FeatureDefinition definition) {
		Check.notNull(definition);
		List<VariableParameter<?>> dependentParameters = Lists.newArrayList();
		for (FeatureDefinitionParameterPair pair : _adjacentNodes.get(definition)) {
			dependentParameters.add(pair.getParameter());
		}
		return dependentParameters;
	}

	@Override
	public Iterator<FeatureDefinition> iterator() {
		return _topologicalOrder.iterator();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FeatureDefinitionGraph) {
			FeatureDefinitionGraph graph = (FeatureDefinitionGraph) o;
			return graph._adjacentNodes.equals(_adjacentNodes) && graph._topologicalOrder.equals(_topologicalOrder);
		}
		return false;
	}

	public int getNumberOfFeatures() {
		return _topologicalOrder.size();
	}

	private boolean containsPath(FeatureDefinition start, FeatureDefinition goal) {
		if (start.equals(goal)) {
			return true;
		}
		for (FeatureDefinitionParameterPair pair : _adjacentNodes.get(start)) {
			if (containsPath(pair.getFeatureDefinition(), goal)) {
				return true;
			}
		}
		return false;
	}

	private void adjustInsertionOrder(FeatureDefinition parentFeature, FeatureDefinition childFeature) {
		int parentPosition = _topologicalOrder.indexOf(parentFeature);
		int childPosition = _topologicalOrder.indexOf(childFeature);
		if (parentPosition > childPosition) {
			_topologicalOrder.remove(parentFeature);
			_topologicalOrder.add(childPosition, parentFeature);
		}
	}

}
