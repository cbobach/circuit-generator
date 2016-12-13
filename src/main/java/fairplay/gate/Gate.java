package fairplay.gate;

import java.util.Collection;

/**
 * Created by cbobach on 27-10-16.
 *
 * Interface for gates
 */
public interface Gate {

    default String toFairPlayString() {
        return getNumberOfInputWires() + " " +
                getNumberOfOutputWires() + " " +
                getLeftWireIndex() + " " +
                getRightWireIndex() + " " +
                getOutputWireIndex() + " " +
                getType();
    }

    int getNumberOfInputWires();

    int getNumberOfOutputWires();

    int getLeftWireIndex();

    int getRightWireIndex();

    int getOutputWireIndex();

    GateType getType();
}
