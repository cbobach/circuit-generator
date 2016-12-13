package fairplay.output;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fairplay.gate.GateConstructor;
import fairplay.parsers.FairplayParser;

import fairplay.common.CommonUtilities;


public class WireReuseCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File circuitFile = new File("test/data/md5_fairplay.txt");
		FairplayParser circuitParser = new FairplayParser(circuitFile, false);
		List<GateConstructor> gates = circuitParser.getGates();
		
		List<Integer> wires = new ArrayList<Integer>();
		
		for(GateConstructor g: gates) {
			wires.add(g.getLeftWireIndex());
			wires.add(g.getRightWireIndex());
		}
		System.out.println(wires.size());
		System.out.println(CommonUtilities.getWireCount(gates));

	}

}
