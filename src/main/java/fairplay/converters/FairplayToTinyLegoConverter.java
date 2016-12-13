package fairplay.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fairplay.gate.GateConstructor;
import fairplay.parsers.CircuitParser;
import fairplay.common.InputGateType;

public class FairplayToTinyLegoConverter implements CircuitConverter<List<GateConstructor>, GateConstructor> {

	private CircuitParser<GateConstructor> circuitParser;
	private CircuitParser<GateConstructor> newCircuitParser;
	private ListToLayersConverter listConverter;
	private List<GateConstructor> gates;

	public FairplayToTinyLegoConverter(CircuitParser<GateConstructor> circuitParser) {
		this.circuitParser = circuitParser;
		gates = circuitParser.getGates();
		int numOfWires = circuitParser.getNumberOfWires();
		int numOfOutputs = circuitParser.getNumberOfOutputs();
		int numOfNonOutputs = numOfWires - numOfOutputs;
		int numberOfAndGates = circuitParser.getNumberOfANDGates();
		List<GateConstructor> identityGates = new ArrayList<GateConstructor>();
		for (GateConstructor g: gates) {
			int currentOutputNumber = g.getOutputWireIndex() - numOfNonOutputs;
			if (currentOutputNumber >= 0) {
				int newOutputIndex = currentOutputNumber + numOfWires;
				GateConstructor gIdentity = new GateConstructor("2 1 " + g.getOutputWireIndex() + " " +
						g.getOutputWireIndex() + " " + newOutputIndex + " 0001", InputGateType.FAIRPLAY);
				gIdentity.setGateNumber(numberOfAndGates+currentOutputNumber);
				identityGates.add(gIdentity);
			}
		}
		gates.addAll(identityGates);
	}
	@Override
	public List<List<GateConstructor>> getGates() {
		
		newCircuitParser = new CircuitParser<GateConstructor>() {

			@Override
			public File getCircuitFile() {
				return circuitParser.getCircuitFile();
			}

			@Override
			public List<GateConstructor> getGates() {
				return gates;
			}

			@Override
			public String[] getHeaders() {
				return circuitParser.getHeaders();
				
			}

			@Override
			public int getNumberOfInputs() {
				return circuitParser.getNumberOfInputs();
			}

			@Override
			public int getNumberOfOutputs() {
				return circuitParser.getNumberOfOutputs();
			}

			@Override
			public int getNumberOfANDGates() {
				return circuitParser.getNumberOfANDGates() + circuitParser.getNumberOfOutputs();
			}

			@Override
			public int getNumberOfP1Inputs() {
				return circuitParser.getNumberOfP1Inputs();
			}

			@Override
			public int getNumberOfP2Inputs() {
				return circuitParser.getNumberOfP2Inputs();
			}

			@Override
			public int getNumberOfWires() {
				return circuitParser.getNumberOfWires() + circuitParser.getNumberOfOutputs();
			}
		};
		
		listConverter = new ListToLayersConverter(newCircuitParser);
		return listConverter.getGates();
//		return gates;
	}

	@Override
	public String[] getHeaders() {
//		int newNumberOfWires = circuitParser.getNumberOfWires() + circuitParser.getNumberOfOutputs(); 
//		int numberOfGates = newNumberOfWires - circuitParser.getNumberOfInputs();
//		String header0 = numberOfGates + " " + newNumberOfWires;
//		String[] currentHeaders = circuitParser.getHeaders();
//		String header1 = currentHeaders[0] + " " + currentHeaders[1] + " " + currentHeaders[2];
//		return new String[]{header0, header1};
		return listConverter.getHeaders();
	}

	@Override
	public CircuitParser<GateConstructor> getCircuitParser() {
		return circuitParser;
	}

}
