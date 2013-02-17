package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import common.CircuitParser;
import common.CommonUtilities;
import common.Gate;
import common.GateTypes;

public class SPACLParser implements CircuitParser<List<Gate>> {

	private File circuitFile;
	private String numberOfleftInput;
	private String numberOfrightInput;
	private String numberOfOutput;
	private String xorMaxlayerSize;
	private String invMaxlayerSize;
	private String andMaxlayerSize;
	private int numberOfLayers;
	private int numberOfNonXorGates;
	private int numberOfWires;

	public SPACLParser(File circuitFile) {
		this.circuitFile = circuitFile;
		numberOfNonXorGates = 0;
	}
	@Override
	public List<List<Gate>> getGates() {
		List<List<Gate>> layersOfGates = new ArrayList<List<Gate>>();
		try {
			BufferedReader fbr = new BufferedReader(new InputStreamReader(
					new FileInputStream(circuitFile), Charset.defaultCharset()));
			String line = fbr.readLine();
			//hack to skip first line
			int i= -1;
			while((line = fbr.readLine()) != null) {
				if (line.isEmpty()){
					continue;
				}
				line = line.trim();
				if (line.contains("max_width_xor")) {
					xorMaxlayerSize = line.substring(14, line.length() - 2);
				} else if (line.contains("max_width_inv")) {
					invMaxlayerSize = line.substring(14, line.length() - 2);
				} else if (line.contains("max_width_and")) {
					andMaxlayerSize = line.substring(14, line.length() - 2);
				} else if (line.contains("max_width_private_common_load")) {
					numberOfleftInput = line.substring(30, line.length() - 2);
				} else if (line.contains("max_width_public_common_load")) {
					numberOfrightInput = line.substring(29, line.length() - 2);
				} else if (line.contains("max_width_public_common_out")) {
					numberOfOutput = line.substring(28, line.length() - 2);
				} else if (line.contains("begin_layer_xor") || 
						line.contains("begin_layer_inv") ||
						line.contains("begin_layer_and")) {
					layersOfGates.add(new ArrayList<Gate>());
					i++;
				} else if (line.startsWith("xor(")) {
					layersOfGates.get(i).add(getGate(line, GateTypes.XOR));
				} else if (line.startsWith("inv(")) {
					layersOfGates.get(i).add(getGate(line, GateTypes.INV));
					numberOfNonXorGates++;
				} else if (line.startsWith("and(")) {
					layersOfGates.get(i).add(getGate(line, GateTypes.AND));
					numberOfNonXorGates++;
				}
			}
			fbr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		numberOfLayers = layersOfGates.size();
		numberOfWires = CommonUtilities.getWireCountList(layersOfGates);
		
		return layersOfGates;
	}

	private Gate getGate(String line, GateTypes type) {
		String gateType = "";
		if (type.equals(GateTypes.XOR)) {
			gateType = "0110";
		} else if (type.equals(GateTypes.INV)) {
			gateType = "-1";
		} else if (type.equals(GateTypes.AND)) {
			gateType = "0001";
		}

		String[] split = line.split(",");
		String output = split[0].substring(4);
		String leftInput = split[1];
		String rightInput = split[2];
		String gateString = "2 1 " + leftInput + " " + rightInput + " " +
				output + " " + gateType;
		return new Gate(gateString);

	}

	@Override
	public String[] getHeaders() {
		int input = Integer.parseInt(numberOfleftInput) + Integer.parseInt(numberOfrightInput);
		int maxLayer = Math.max(Integer.parseInt(xorMaxlayerSize), Integer.parseInt(invMaxlayerSize));
		maxLayer = Math.max(maxLayer, Integer.parseInt(andMaxlayerSize));
		
		return new String[]{input + " " + numberOfOutput + " " + numberOfWires +
				" " + numberOfLayers + " " + maxLayer + " " + numberOfNonXorGates};
	}

}