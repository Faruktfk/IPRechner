package ipRechner;

import java.util.Arrays;
import javax.swing.JOptionPane;

public class MusterIP_Rechner {

	public static void main(String[] args) {
		String input = JOptionPane.showInputDialog("Adresse Eingeben z.B. 192.168.100.10 /24: ");
		if (input.isEmpty())
			input = "192.168.100.10/23";
		System.out.println("Input : " + input);
		String[] parts = input.split("[/]");
		String[] octetts = parts[0].split("[.]");
		int[] ip = new int[4];
		for (int i = 0; i < 4; i++) {
			ip[i] = Integer.parseInt(octetts[i].strip());
		}
		int netmask = Integer.parseInt(parts[1].strip());
		String nms = nmToString(netmask);
		System.out.println("Netzmaske dezimal: " + nms);
		System.out.println("Größe : " + (int) (Math.pow(2, 32 - netmask) - 2) + " gültige IPs.");
		octetts = nms.split("[.]");
		int[] nm = new int[4];
		for (int i = 0; i < 4; i++) {
			nm[i] = Integer.parseInt(octetts[i].strip());
		}
		int[] netid = new int[4];
		int[] bc = new int[4];
		for (int i = 0; i < 4; i++) {
			netid[i] = (ip[i] & nm[i]) & 0xFF;
			bc[i] = (ip[i] | ~nm[i]) & 0xFF;
		}
		System.out.println("NETID : " + Arrays.toString(netid).replaceAll("[,]", ".").replaceAll("[ \\[\\]]", ""));
		System.out.println("BC    : " + Arrays.toString(bc).replaceAll("[,]", ".").replaceAll("[ \\[\\]]", ""));
		netid[3]++;
		bc[3]--;
		System.out.println("VON   : " + Arrays.toString(netid).replaceAll("[,]", ".").replaceAll("[ \\[\\]]", ""));
		System.out.println("BIS   : " + Arrays.toString(bc).replaceAll("[,]", ".").replaceAll("[ \\[\\]]", ""));
		System.out.println("Def.GW: " + Arrays.toString(bc).replaceAll("[,]", ".").replaceAll("[ \\[\\]]", ""));
	}

	public static String nmToString(int nm) {
		String output = ".";
		if (nm >= 0 && nm <= 32) {
			output = "";
			int full = nm / 8;
			int r = nm % 8;
			for (int i = 0; i < 4; i++) {
				if (i < full)
					output += "255.";
				else if (r != 0) {
					String bin = "";
					for (int j = 0; j < 8; j++) {
						if (j < r)
							bin += "1";
						else
							bin += "0";
					}
					output += Integer.parseInt(bin, 2) + ".";
					r = 0;
				} else
					output += "0.";
			}
		}
		return output.substring(0, output.length() - 1);
	}
}
