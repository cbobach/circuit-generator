package fairplay.generators;

import fairplay.gate.ANDGate;
import fairplay.gate.Gate;
import fairplay.gate.NOTGate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbobach on 25-11-16.
 *
 * Creating ID circuit of AND gates, resulting in only one layer and half the amount of gates.
 */
public class OptimizedIdentityCircuitGenerator extends ExpandableCircuitGenerator {

    public OptimizedIdentityCircuitGenerator(int inputSize, int securityParameter, String outputFile) {
        this.outputFile = outputFile;

        int keySize = inputSize;

        numberOfCircuitInputWiresConstructor
                = numberOfCircuitInputWiresEvaluator
                = inputSize + keySize + securityParameter;

        numberOfCircuitOutputWires
                = numberOfCircuitOutputWiresConstructor
                = numberOfCircuitOutputWiresEvaluator
                = numberOfCircuitInputWiresConstructor + numberOfCircuitInputWiresEvaluator;

        firstOutputWire = numberOfCircuitInputWiresConstructor + numberOfCircuitInputWiresEvaluator;

        circuit = generateCircuit();
    }

    private List<Gate> generateCircuit() {
        List<Gate> circuit = new ArrayList<>();

        int inputWires = numberOfCircuitInputWiresConstructor + numberOfCircuitInputWiresEvaluator;
        int nextOutputWireIndex = firstOutputWire;

        for (int i = 0; i < inputWires; i++) {
            int leftInputWire = i, rightInputWire = i;
            Gate gate = new ANDGate(leftInputWire, rightInputWire, nextOutputWireIndex++);
            circuit.add(gate);
        }

        numberOfGates = circuit.size();
        numberOfWires = numberOfGates * 2;

        return circuit;
    }
}
