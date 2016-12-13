package fairplay.converters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fairplay.gate.GateConstructor;
import fairplay.parsers.FairplayParser;

import fairplay.parsers.CircuitParser;
import fairplay.common.CommonUtilities;
import fairplay.gate.GateType;
import fairplay.common.LayerComparator;
import fairplay.common.TopoTypeComparator;


public class FairplayToSPACLConverter implements CircuitConverter<List<GateConstructor>, GateConstructor> {

	CircuitConverter<List<GateConstructor>, GateConstructor> circuitConverter;
	private String[] header;
	private int[] circuitInfo;
	private int heapSize;
	private int[] widthSizes;
	
	public FairplayToSPACLConverter(CircuitConverter<List<GateConstructor>, GateConstructor> circuitConverter) {
		this.circuitConverter = circuitConverter;
		header = new String[1];
		circuitInfo = new int[4];
	}
	
	@Override
	public List<List<GateConstructor>> getGates() {
		List<List<GateConstructor>> gates = circuitConverter.getGates();
		FairplayParser circuitParser = (FairplayParser) circuitConverter.getCircuitParser();
		
		circuitInfo[0] = circuitParser.getNumberOfP1Inputs();
		circuitInfo[1] = circuitParser.getNumberOfP2Inputs();
		circuitInfo[2] = circuitParser.getNumberOfInputs();
		circuitInfo[3] = circuitParser.getNumberOfOutputs();
		List<GateConstructor> sortedGates = getLayeredGates(gates, circuitInfo[2]);
		
		gates = getSortedGates(sortedGates);
		widthSizes = getWidthSizes(gates);
		heapSize = CommonUtilities.getWireCountList(gates);
		header[0] = Integer.toString(circuitInfo[2]) + " " + Integer.toString(circuitInfo[3]) +
				" " + Integer.toString(heapSize);
		
		return gates;
	}
	
	@Override
	public String[] getHeaders() {
		return header;
	}
	
	@Override
	public CircuitParser<GateConstructor> getCircuitParser() {
		return circuitConverter.getCircuitParser();
	}

	public int[] getCircuitInfo() {
		int[] res = new int[7];
		res[0] = circuitInfo[0];
		res[1] = circuitInfo[1];
		res[2] = circuitInfo[3];
		res[3] = heapSize;
		res[4] = widthSizes[0];
		res[5] = widthSizes[1];
		res[6] = widthSizes[2];
		
		return res;
	}

	private List<GateConstructor> getLayeredGates(List<List<GateConstructor>> gates, int inputSize) {
		List<GateConstructor> res = new ArrayList<GateConstructor>();

		// Init wireLayer map with input wires
		HashMap<Integer, Integer> wireLayers = new HashMap<Integer, Integer>();
		for (int i = 0; i < inputSize; i++) {
			wireLayers.put(i, 0);
		}

		for (List<GateConstructor> list: gates) {
			for (GateConstructor g: list) {
				int topologicalLayer = 0;
				if (g.isAND() || g.isXOR()) {
					int a = wireLayers.get(g.getLeftWireIndex());
					int b = wireLayers.get(g.getRightWireIndex());
					if (g.isAND()) {
						topologicalLayer = Math.max(a, b) + 1;
						g.setTopologicalLayer(topologicalLayer - 1);
					} else if (g.isXOR()) {
						topologicalLayer = Math.max(a, b);
						g.setTopologicalLayer(topologicalLayer);
					}
				} else if (g.isINV()) {
					topologicalLayer = wireLayers.get(g.getLeftWireIndex());
					g.setTopologicalLayer(topologicalLayer);
				} else {
					System.out.println("Input Circuit may only consist of XOR, INV, AND");
					System.out.println("Terminating without fairplay.output");
					System.exit(-1);
				}
				wireLayers.put(g.getOutputWireIndex(), topologicalLayer);

				res.add(g);
			}
		}
		Collections.sort(res, new TopoTypeComparator());
		return res;
	}

	private List<List<GateConstructor>> getSortedGates(List<GateConstructor> gates) {
		List<List<GateConstructor>> res = new ArrayList<List<GateConstructor>>();
		GateType current = null;
		int index = 0;
		GateConstructor trial = gates.get(0);

		if (trial.isXOR()) {
			current = GateType.XOR;
		} else if (trial.isAND()) {
			current = GateType.AND;
		} else if (trial.isINV()) {
			current = GateType.INV;
		}

		res.add(new ArrayList<GateConstructor>());
		for (GateConstructor g: gates) {
			if (equal(g, current)) {
				res.get(index).add(g);
			} else {
				res.add(new ArrayList<GateConstructor>());
				index++;
				res.get(index).add(g);
				current = getNewCurrent(g);
			}
		}

		for (List<GateConstructor> list: res) {
			Collections.sort(list, new LayerComparator());
		}
		return res;
	}

	private boolean equal(GateConstructor g, GateType current) {
		if (g.isXOR() && current == GateType.XOR) {
			return true;
		} else if (g.isAND() && current == GateType.AND) {
			return true;
		} else if (g.isINV() && current == GateType.INV) {
			return true;
		} else return false;
	}

	private GateType getNewCurrent(GateConstructor g) {
		GateType res = null;
		if (g.isXOR()) {
			res = GateType.XOR;
		} else if (g.isAND()) {
			res = GateType.AND;
		} else if (g.isINV()) {
			res = GateType.INV;
		}
		return res;
	}

	private int[] getWidthSizes(List<List<GateConstructor>> gates) {
		int[] res = new int[3]; //XOR = 0, INV = 1, AND = 2
		for (List<GateConstructor> list: gates) {
			GateConstructor tester = list.get(0);
			if (tester.isXOR()) {
				res[0] = Math.max(res[0], list.size());
			} else if (tester.isINV()) {
				res[1] = Math.max(res[1], list.size());
			} else if (tester.isAND()) {
				res[2] = Math.max(res[2], list.size());
			}
		}
		return res;
	}
}
