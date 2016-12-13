package fairplay.generators;

import fairplay.gate.ANDGate;
import fairplay.gate.Gate;
import fairplay.gate.XORGate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbobach on 27-10-16.
 *
 * Creates an tag circuit, based on inputs x, s and k outputs a tag t.
 * x and k have the same length, the size of s is the security parameter l = |s|.
 *
 * We construct a matrix M_k
 * 1. row:   k_1    ...                 k_n
 * 2. row:   s_l    k_1  ...            k_n-1
 * 3. row:   s_l-1  s_l  k_1  ...       k_n-2
 * ...
 * l'th row: s_1    ...  s_l  k_1  ...  k_n-s
 *
 * Then M_k * x => t
 */
public class TagCircuitGenerator extends ExpandableCircuitGenerator {

    private final int inputSize;
    private final int keySize;
    private final int securityParameter;

    /**
     * @param inputSize Input size = key size
     * @param securityParameter Security parameter < input size
     * @param outputFile name and extension on the desired output file
     */
    public TagCircuitGenerator(int inputSize, int securityParameter, String outputFile) {
        this.inputSize = inputSize;
        this.securityParameter = securityParameter;
        super.outputFile = outputFile;

        keySize = this.inputSize;

        numberOfCircuitInputWiresConstructor = inputSize + keySize + securityParameter;
        numberOfCircuitInputWiresEvaluator = inputSize + keySize + securityParameter;

        numberOfCircuitOutputWires
                = numberOfCircuitOutputWiresConstructor
                = numberOfCircuitOutputWiresEvaluator
                = securityParameter + 1;

        numberOfGates = ((inputSize + keySize) * 2 - 1) * (securityParameter + 1);
        numberOfWires = numberOfGates + (inputSize + keySize + securityParameter) * 2;
        firstOutputWire = numberOfWires - (securityParameter + 1);

        circuit = generateGateList();
    }

    /**
     * On inputs generates a circuit with 2 * inputSize + securityParameter, input wires
     * and inputSize * securityParameter, fairplay.output wires.
     *
     * Input and key size are assumed to be equal.
     * Security parameter is assumed smaller than input size
     *
     * @return List<GateConstructor> circuit
     */
    public List<Gate> generateGateList() {
        List<Gate> circuit = new ArrayList<>();

        int firstANDOutputWire = (inputSize + keySize + securityParameter) * 2;
        int nextInternalOutputWire = firstANDOutputWire;
        int leftXORInputWire = firstANDOutputWire;
        int rightXORInputWire = firstANDOutputWire + 1;
        int nextExternalOutputWire = firstOutputWire;

//        Running over the security parameter, aka the height of the matrix M_k
//        this is the same as securityParameter + 1
        for (int s = 0; s <= securityParameter; s++) {

//            Adding all AND gates to the circuit
            for (int i = 0; i < inputSize; i++) {
//                Right input wire is indexed from inputSize + i + s ... inputSize + inputSize + s
                int leftInputWire = i;
                int rightInputWire = inputSize + i + s;

                Gate gate1 = new ANDGate(leftInputWire, rightInputWire, nextInternalOutputWire);

                leftInputWire = inputSize + keySize + securityParameter + leftInputWire;
                rightInputWire = inputSize + keySize + securityParameter + rightInputWire;

                Gate gate2 = new ANDGate(leftInputWire, rightInputWire, ++nextInternalOutputWire);

                circuit.add(gate1);
                circuit.add(gate2);

                nextInternalOutputWire++;
            }


//            Adding all the XOR gates to the circuit
            for (int j = 0; j < inputSize + keySize - 1; j++) {

                if(j == inputSize + keySize - 2) {
                    Gate gate = new XORGate(leftXORInputWire, rightXORInputWire, nextExternalOutputWire);
                    circuit.add(gate);

                    leftXORInputWire = nextInternalOutputWire;
                    rightXORInputWire++;
                    nextExternalOutputWire++;
                } else {
                    Gate gate = new XORGate(leftXORInputWire, rightXORInputWire, nextInternalOutputWire);
                    circuit.add(gate);

                    leftXORInputWire = nextInternalOutputWire;
                    rightXORInputWire++;
                    nextInternalOutputWire++;
                }
            }

            rightXORInputWire += inputSize * 2;
            leftXORInputWire++;
        }

        return circuit;
    }
}
