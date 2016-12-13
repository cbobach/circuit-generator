package fairplay.converters;

import fairplay.parsers.CircuitParser;
import fairplay.common.CircuitProvider;

public interface CircuitConverter<E, T> extends CircuitProvider<E> {
	public CircuitParser<T> getCircuitParser();
}
