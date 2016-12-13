package fairplay.converters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.MultiValueMap;

import fairplay.parsers.CircuitParser;
import fairplay.common.OutputWireComparator;

/**
 * @author Roberto Trifiletti
 *
 */
public class ListToLayersConverter implements CircuitConverter<List<GateConstructor>, GateConstructor> {

	private MultiValueMap leftMap;
	private MultiValueMap rightMap;
	private HashMap<Integer, Integer> replacementMap;
	private List<List<GateConstructor>> layersOfGates;
	private int currentWireIndex;

	private CircuitParser<GateConstructor> circuitParser;

	/**
	 * @param circuitFile
	 * @param outputFile
	 */
	public ListToLayersConverter(CircuitParser<GateConstructor> circuitParser) {
		this.circuitParser = circuitParser;

		leftMap = new MultiValueMap();
		rightMap = new MultiValueMap();
		replacementMap = new HashMap<Integer, Integer>();
	}
	
	@Override
	public List<List<GateConstructor>> getGates() {
		List<GateConstructor> gates = circuitParser.getGates();
		currentWireIndex = circuitParser.getNumberOfInputs();
		layersOfGates = getLayersOfGates(gates);

		int startOutputWire = circuitParser.getNumberOfWires() - circuitParser.getNumberOfOutputs();
		replaceWires(layersOfGates, circuitParser.getNumberOfInputs(), 
				startOutputWire);
		
		return layersOfGates;
	}
	
	@Override
	public String[] getHeaders() {
		int actualNumberOfWires = circuitParser.getNumberOfWires();

		//We have to figure out the max layer size before writing to the file.
		int maxLayerWidth = 0;
		for (List<GateConstructor> l: layersOfGates) {
			maxLayerWidth = Math.max(maxLayerWidth, l.size());
		}

		int totalNumberOfInputs = circuitParser.getNumberOfInputs();
		int totalNumberOfOutputs = circuitParser.getNumberOfOutputs();
		int numberOfNonXORGates = circuitParser.getNumberOfANDGates();

		return new String[]{totalNumberOfInputs + " " + totalNumberOfOutputs + " " +
				actualNumberOfWires + " " + layersOfGates.size() + " " + maxLayerWidth + " " +
				numberOfNonXORGates};
	}

	@Override
	public CircuitParser<GateConstructor> getCircuitParser() {
		return circuitParser;
	}

	/**
	 * @param gates
	 * @return A lists of lists where each list represents a layer of gates in
	 * the converted circuit
	 */
	@SuppressWarnings("unchecked")
	private List<List<GateConstructor>> getLayersOfGates(List<GateConstructor> gates) {
		List<List<GateConstructor>> layersOfGates = new ArrayList<List<GateConstructor>>();
		initMaps(gates);

		int totalNumberOfInputs = circuitParser.getNumberOfInputs();
		/*
		 * Loop to run through each list in our MultiMap, first runs through all
		 * gates with left input 0, 1, 2, ..., #inputs.
		 * For each of these "input" dependant gates, we visit them recursively
		 * and set a timestamp on each of these.
		 */
		for (int i = 0; i < totalNumberOfInputs; i++) {
			Collection<GateConstructor> leftList = leftMap.getCollection(i);
			if(leftList == null){
				continue;
			}
			for (GateConstructor g: leftList) {
				visitGate(g, 0, layersOfGates);
			}
		}

		/*
		 * Now that we've visited all gates which depends on a left input, we
		 * do the same for the right input and recursively visit them again.
		 * When we visit a gate which has already been visited we set a
		 * time stamp again to be the max of the current time and the time stamp
		 * of the gate. This value determines which layer the gate is to be
		 * placed in. 
		 */
		for (int i = 0; i < totalNumberOfInputs; i++) {
			Collection<GateConstructor> rightList = rightMap.getCollection(i);
			if (rightList == null) {
				continue;
			}
			for (GateConstructor g: rightList) {
				layersOfGates = visitGate(g, 0, layersOfGates);
			}
		}
		return layersOfGates;
	}

	/*
	 * We fill up our auxiliary maps which will help us find gates which are
	 * depending on a given gate. These Maps are MultiValued, so if two
	 * elements have the same key a list is created to hold each value associated to this
	 * key.
	 */
	private void initMaps(List<GateConstructor> gates) {
		for (GateConstructor g: gates) {
			leftMap.put(g.getLeftWireIndex(), g);
			if (g.getNumberOfInputWires() == 2) {
				rightMap.put(g.getRightWireIndex(), g);
			}
		}
	}

	/**
	 * @param g
	 * @param time
	 * @param layersOfGates
	 * @return A list of lists representing each layer in the converted circuit
	 */
	private List<List<GateConstructor>> visitGate(GateConstructor g, int time, List<List<GateConstructor>> layersOfGates) {

		//update gates to outputWireIndex (increment), then run through all dependents and rename these.
		g.decCounter();
		g.setLayer(time);
		if (g.getCounter() == 0) {
			addToSublist(g, layersOfGates);
			for (GateConstructor dependingGate: getDependingGates(g)) {
				visitGate(dependingGate, g.getLayer() + 1, layersOfGates);
			}
		}
		return layersOfGates;
	}

	private void replaceWires(List<List<GateConstructor>> layersOfGates, int numberOfInputs,
							  int startOutputWire) {
		for (List<GateConstructor> list: layersOfGates) {
			for (GateConstructor g: list) {
				int leftIndex = g.getLeftWireIndex();
				int rightIndex = g.getRightWireIndex();
				int outputWireIndex = g.getOutputWireIndex();
				if (outputWireIndex < startOutputWire) {
					int newIndex = currentWireIndex++;
					g.setOutputWireIndex(newIndex);
					replacementMap.put(outputWireIndex, newIndex);
				} else {
					replacementMap.put(outputWireIndex, outputWireIndex);
				}
				
				if (leftIndex >= numberOfInputs) {
					g.setLeftWireIndex(replacementMap.get(leftIndex));
				}

				if (rightIndex >= numberOfInputs) {
					g.setRightWireIndex(replacementMap.get(rightIndex));
				}
			}
			Collections.sort(list, new OutputWireComparator());
		}
	}

	/**
	 * @param g
	 * @return A list of all gates depending directly on the given gate
	 */
	@SuppressWarnings("unchecked")
	private List<GateConstructor> getDependingGates(GateConstructor g) {
		List<GateConstructor> res = new ArrayList<GateConstructor>();
		int inputIndex = g.getOutputWireIndex();
		Collection<GateConstructor> leftList = leftMap.getCollection(inputIndex);
		Collection<GateConstructor> rightList = rightMap.getCollection(inputIndex);

		if (leftList != null){
			res.addAll(leftList);
		}

		if (rightList != null){
			res.addAll(rightList);
		}

		return res;
	}

	/**
	 * @param g
	 * @param res
	 * @return A List of lists where the given gate has been added to the
	 * correct sublists depending on it's timestamp
	 */
	private List<List<GateConstructor>> addToSublist(GateConstructor g, List<List<GateConstructor>> layersOfGates) {
		while (layersOfGates.size() <= g.getLayer()) {
			layersOfGates.add(new ArrayList<GateConstructor>());
		}

		List<GateConstructor> layer = layersOfGates.get(g.getLayer());
		layer.add(g);

		return layersOfGates;
	}
}
