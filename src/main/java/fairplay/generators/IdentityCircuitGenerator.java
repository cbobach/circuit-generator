package fairplay.generators;

import fairplay.gate.Gate;
import fairplay.gate.NOTGate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbobach on 31-10-16.
 *
 * Generates an identity circuit
 */
public class IdentityCircuitGenerator extends ExpandableCircuitGenerator {

    private final int numberOfInputWires;

    public IdentityCircuitGenerator(int inputSize, int securityParameter, String outputFile) {
        this.outputFile = outputFile;

        int keySize = inputSize;

        numberOfCircuitInputWiresConstructor
                = numberOfCircuitInputWiresEvaluator
                = inputSize + keySize + securityParameter;

        numberOfInputWires = numberOfCircuitInputWiresConstructor + numberOfCircuitInputWiresEvaluator;
        numberOfCircuitOutputWires
                = numberOfCircuitOutputWiresConstructor
                = numberOfCircuitOutputWiresEvaluator
                = numberOfInputWires;

        numberOfGates = numberOfInputWires * 2;
        numberOfWires = numberOfInputWires + numberOfGates;
        firstOutputWire = numberOfWires - numberOfInputWires;

        circuit = generateCircuit();
    }

    private List<Gate> generateCircuit() {
        List<Gate> circuit = new ArrayList<>();

        int nextOutputWire = numberOfCircuitInputWiresConstructor + numberOfCircuitInputWiresEvaluator;

//        Constructing negated layer
        for (int i = 0; i < numberOfInputWires; i++) {
            Gate gate = new NOTGate(i, nextOutputWire);
            circuit.add(gate);

            nextOutputWire++;
        }

//        Constructing negated layer
        for (int i = numberOfInputWires; i < 2 * numberOfInputWires; i++) {
            Gate gate = new NOTGate(i, nextOutputWire);
            circuit.add(gate);

            nextOutputWire++;
        }

        return circuit;
    }
}
