package fairplay.gate;

/**
 * Created by cbobach on 31-10-16.
 *
 * Implements the functionality of a NOT gate
 */
public class NOTGate  implements Gate {
    private final GateType type = GateType.INV;
    private final int numberOfInputWires = 1;
    private final int numberOfOutputWires = 1;

    private final int inputWire;
    private final int outputWire;

    public NOTGate(int inputWire, int outputWire) {
        this.inputWire = inputWire;
        this.outputWire = outputWire;
    }

    @Override
    public GateType getType() {
        return type;
    }

    @Override
    public int getNumberOfInputWires() {
        return numberOfInputWires;
    }

    @Override
    public int getNumberOfOutputWires() {
        return numberOfOutputWires;
    }

    @Override
    public int getLeftWireIndex() {
        return inputWire;
    }

    @Override
    public int getRightWireIndex() {
        return inputWire;
    }

    @Override
    public int getOutputWireIndex() {
        return outputWire;
    }

    public String toFairPlayString() {
        return getNumberOfInputWires() + " " +
                getNumberOfOutputWires() + " " +
                getLeftWireIndex() + " " +
                getOutputWireIndex() + " " +
                getType();
    }
}
