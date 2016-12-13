package fairplay.gate;

/**
 * Created by cbobach on 27-10-16.
 *
 * Implements AND gate
 */
public class ANDGate implements Gate {
    private final GateType type = GateType.AND;
    private final int numberOfInputWires = 2;
    private final int numberOfOutputWires = 1;

    private final int leftWireIndex;
    private final int rightWireIndex;
    private final int outputWireIndex;

    public ANDGate(int leftWireIndex, int rightWireIndex, int outputWireIndex) {
        this.leftWireIndex = leftWireIndex;
        this.rightWireIndex = rightWireIndex;
        this.outputWireIndex = outputWireIndex;
    }

    @Override
    public int getNumberOfInputWires() { return numberOfInputWires; }

    @Override
    public int getNumberOfOutputWires() {
        return numberOfOutputWires;
    }

    @Override
    public int getLeftWireIndex() {
        return leftWireIndex;
    }

    @Override
    public int getRightWireIndex() {
        return rightWireIndex;
    }

    @Override
    public int getOutputWireIndex() {
        return outputWireIndex;
    }

    @Override
    public GateType getType() {
        return type;
    }
}
