package fairplay.converters;

import java.util.ArrayList;
import java.util.List;

import fairplay.gate.GateConstructor;
import fairplay.parsers.FairplayParser;

import fairplay.parsers.CircuitParser;
import fairplay.common.CommonUtilities;
import fairplay.common.InputGateType;

public class FairplayToAugConverter implements CircuitConverter<GateConstructor, GateConstructor> {

	private FairplayParser circuitParser;
	private int numberOfNonXORGatesAdded;
	private int largestOutputWire;
	private int largestAugOutputWire;
	private int l;
	private List<GateConstructor> augCircuit;
	private List<GateConstructor> outputGates;
	private int numberOfOriginalInputs;
	private int t_a;
	private int numberOfNewInputs;
	private int r;

	public FairplayToAugConverter(FairplayParser circuitParser, int l) {
		this.circuitParser = circuitParser;
		this.l = l;
		
		outputGates = new ArrayList<GateConstructor>();
		numberOfNonXORGatesAdded = 0;
		largestOutputWire = 0;
	}
	
	@Override
	public List<GateConstructor> getGates() {
		List<GateConstructor> parsedGates = circuitParser.getGates();
		
		// Must be set after call to circuitParser
		numberOfOriginalInputs = circuitParser.getNumberOfInputs();
		t_a = circuitParser.getNumberOfP1Inputs();
		numberOfNewInputs = numberOfOriginalInputs + t_a + 2 * l;
		r = numberOfNewInputs - t_a - l;

		List<GateConstructor> augGates = getAugGates();
		List<GateConstructor> incrementedGates =
				getIncrementedGates(parsedGates);
		List<GateConstructor> augOutputGates = getAugOutputGates(); //Must be called after getIncrementedGates()

		augCircuit = new ArrayList<GateConstructor>();
		augCircuit.addAll(augGates);
		augCircuit.addAll(incrementedGates);
		augCircuit.addAll(augOutputGates);
		
		return augCircuit;
	}
	
	@Override
	public String[] getHeaders() {
		String[] res = new String[2];

		res[0] = augCircuit.size() + " " + (CommonUtilities.getWireCount(augCircuit) + 1);

		int newP1Input = circuitParser.getNumberOfP1Inputs() + l;
		int newP2Input = circuitParser.getNumberOfP2Inputs() + t_a + l;
		int newP1Output = circuitParser.getNumberOfP1Outputs();
		int newP2Output = circuitParser.getNumberOfP2Outputs() + l;

		res[1] = newP1Input + " " + newP2Input + " " + newP1Output + " " +
				newP2Output;

		return res;
	}

	@Override
	public CircuitParser<GateConstructor> getCircuitParser() {
		return circuitParser;
	}


	// TODO: 26-10-16 This is the interresting part.
	private List<GateConstructor> getAugGates() {
		List<GateConstructor> res = new ArrayList<GateConstructor>();
		int gateNumber = 0;
		
		List<GateConstructor> andGates = new ArrayList<GateConstructor>();

		/**
		 * Add all the AND gates. We assume r is the 3rd input
		 */
		for (int i = 0; i < l; i++) {
			for (int x = 0; x < t_a; x++) {
				int leftWire = r + i + x;
				int rightWire = x;
				int outputWire = numberOfNewInputs + i * t_a + x;
				GateConstructor g = new GateConstructor("2 " +
						"1 "+
						leftWire + " " +
						rightWire + " " +
						outputWire +
						" 0001",
						InputGateType.FAIRPLAY);
				largestAugOutputWire = Math.max(largestAugOutputWire, g.getOutputWireIndex());
				g.setGateNumber(gateNumber++);
				andGates.add(g);
			}
			res.addAll(andGates);

			numberOfNonXORGatesAdded += andGates.size();
			andGates.clear();
		}

		/**
		 * Add all the XOR gates (both for inner product and final xor).
		 */
		List<GateConstructor> xorGates = new ArrayList<GateConstructor>();
		//How far we filled up the res array with andGates
		int xorWireStart = largestAugOutputWire + 1;
		for (int i = 0; i < l; i++) {
			int priorOutputWire = 0;
			
			for (int x = 1; x < t_a; x++) { // Start x from 1 because we need one less xor per layer
				int leftWire;
				int rightWire = numberOfNewInputs + t_a * i + x;
				if (x == 1){
					leftWire = numberOfNewInputs + t_a * i + x - 1;
				} else {
					leftWire = priorOutputWire;
				}

				int outputWire = xorWireStart + (x - 1) + i * (t_a - 1);
				priorOutputWire = outputWire;

				// We make each xor dependant on the following xor, thus
				// creating a chain structure. The last gate in this list is the
				// fairplay.output gate.
				GateConstructor g = new GateConstructor("2 1 " + leftWire + " " +
						rightWire + " " + outputWire + " 0110", InputGateType.FAIRPLAY);
				largestAugOutputWire = Math.max(largestAugOutputWire, g.getOutputWireIndex());
				xorGates.add(g);
			}

			//We identify the last xor-gate of the chain and xor this
			//with the current bit of s. The fairplay.output of this xor is the s'th
			//fairplay.output bit of the augmentation fairplay.output.
			GateConstructor xorOutputGate = xorGates.get(xorGates.size() - 1);
			int xorOutputWireIndex = xorOutputGate.getOutputWireIndex();

			int s = t_a + i;
			GateConstructor outputGate = new GateConstructor("2 1 " + xorOutputWireIndex + " " +
					s + " " + i + " 0110", InputGateType.FAIRPLAY);

			outputGates.add(outputGate);
			res.addAll(xorGates);
			xorGates.clear();
		}
		return res;

	}

	private List<GateConstructor> getIncrementedGates(List<GateConstructor> parsedGates){
		List<GateConstructor> res = new ArrayList<GateConstructor>();
		int incNumber = largestAugOutputWire + 1 - numberOfOriginalInputs;

		for (GateConstructor g: parsedGates) {
			if(!g.isXOR()){
				g.setGateNumber(g.getGateNumber() + numberOfNonXORGatesAdded);
			}

			//If left wire is y, increment it, else increment wires with new added wire size
			int leftWireIndex = g.getLeftWireIndex();
			if (leftWireIndex < numberOfOriginalInputs &&
					leftWireIndex > t_a - 1) {
				g.setLeftWireIndex(leftWireIndex + l);
			} else if (leftWireIndex >= numberOfOriginalInputs) {
				g.setLeftWireIndex(leftWireIndex + incNumber);
			}

			// Same, but for right wire
			int rightWireIndex = g.getRightWireIndex();
			if (rightWireIndex < numberOfOriginalInputs &&
					rightWireIndex > t_a - 1) {
				g.setRightWireIndex(rightWireIndex + l);
			} else if (rightWireIndex >= numberOfOriginalInputs) {
				g.setRightWireIndex(g.getRightWireIndex() + incNumber);
			}

			// Output wires should always be incremented
			int newIndex = g.getOutputWireIndex() + incNumber;
			g.setOutputWireIndex(newIndex);
			largestOutputWire = Math.max(largestOutputWire, g.getOutputWireIndex());
			res.add(g);
		}
		return res;
	}

	private List<GateConstructor> getAugOutputGates(){
		int incNumber = largestOutputWire + 1;
		for (GateConstructor g: outputGates) {
			g.setOutputWireIndex(g.getOutputWireIndex() + incNumber);
		}
		return outputGates;
	}
}

