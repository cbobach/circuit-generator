package fairplay.gate;

/**
 * Created by cbobach on 29-10-16.
 * Implements XOR gate
 */
public class XORGate implements Gate{
    private final GateType type = GateType.XOR;
    private final int numberOfInputWires = 2;
    private final int numberOfOutputWires = 1;

    private final int leftWireIndex;
    private final int rightWireIndex;
    private final int outputWireIndex;

    public XORGate(int leftWireIndex, int rightWireIndex, int outputWireIndex) {
        this.leftWireIndex = leftWireIndex;
        this.rightWireIndex = rightWireIndex;
        this.outputWireIndex = outputWireIndex;
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
