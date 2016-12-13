package fairplay.common;

import fairplay.gate.GateConstructor;

import java.util.Comparator;

public class OutputWireComparator implements Comparator<GateConstructor> {

	@Override
	public int compare(GateConstructor g1, GateConstructor g2) {
		return g1.getOutputWireIndex() - g2.getOutputWireIndex();
	}

}
