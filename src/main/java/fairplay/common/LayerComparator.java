package fairplay.common;

import fairplay.gate.GateConstructor;

import java.util.Comparator;

public class LayerComparator implements Comparator<GateConstructor> {

	@Override
	public int compare(GateConstructor g1, GateConstructor g2) {
      return g1.getLayer() - g2.getLayer();
	}

}
