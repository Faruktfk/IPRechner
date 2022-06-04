package ipRechner;

public class IPRechner {

	private static final String ERROR_IPMSG = "*Ungültige IP-Adresse eingegeben!",
			ERROR_NM1MSG = "*Ungültige Netzmaske!", ERROR_NM2MSG = ERROR_NM1MSG;// "*Ungültige Netzmaske2!";

	private boolean error;
	private IP ip, nm, nID, bc;

	public IPRechner() {
		error = true;
	}

	public IP getNM() {
		return nm;
	}
	
	public IP getIP() {
		return ip;
	}

	public IP getNID() {
		return nID;
	}
	
	public IP getBC() {
		return bc;
	}
	
	public void resetAll() {
		error = true;
		ip = null;
		nm = null;
		nID = null;
		bc = null;
	}

	public String überprüfeEingabe(String eingabe, int eingabeTyp) {

		if (eingabeTyp == IP.IP_ADRESSE_DECIMAL) {
			ip = new IP(eingabe, IP.IP_ADRESSE_DECIMAL);
			if (ip == null || ip.getValid() == false) {
				ip = null;
				error = true;
				return ERROR_IPMSG;
			}

		} else if (eingabeTyp == IP.IP_ADRESSE_BINARY) {
			ip = new IP(eingabe, IP.IP_ADRESSE_BINARY);
			if (ip == null || ip.getValid() == false) {
				ip = null;
				error = true;
				return ERROR_IPMSG;
			}
						
		} else if (eingabeTyp == IP.NM_LANG) {
			nm = new IP(eingabe, IP.NM_LANG);
			if (nm == null || nm.getValid() == false) {
				nm = null;
				error = true;
				return ERROR_NM1MSG;
			}

		} else if (eingabeTyp == IP.NM_KURZ) {
			try {
				nm = new IP(Integer.parseInt(eingabe), IP.DONT_INVERT);
			} catch (Exception e) {
				nm = null;
			}

			if (nm == null || nm.getValid() == false) {
				nm = null;
				error = true;
				return ERROR_NM2MSG;
			}
		}
		error = false;
		return null;
	}

	public String[] calculateOther() {
		if (error || ip == null || nm == null) {
			return null;
		}

		String[] output = new String[7];
		output[0] = nm.getDecimal();
		output[1] = nm.getNumberOf1() + "";

		if (32 - nm.getNumberOf1() <= 0) {

			output[2] = ip.getDecimal();
			output[3] = ip.getDecimal();
			output[4] = ip.getDecimal();
			output[5] = "0";
			output[6] = ip.getDecimal();

		} else {

			nID = compareBits(true);
			bc = compareBits(false);
			output[2] = nID.getDecimal();
			output[3] = bc.getDecimal();
			output[4] = bc.getNextIP(false).getDecimal();
			output[5] = (long) (Math.pow(2, (32 - nm.getNumberOf1()))) - 2 + "";
			output[6] = nID.getNextIP(true).getDecimal() + " - " + bc.getNextIP(false).getDecimal();

		}

		return output;

	}

	private IP compareBits(boolean isUnd) {
		String output = "";
		IP tempNM = new IP(nm.getNumberOf1(), !isUnd);
		for (int i = 0; i < ip.getBinary().length(); i++) {
			if (isUnd) {
				if (ip.getBinary().charAt(i) == '1' && tempNM.getBinary().charAt(i) == '1') {
					output += '1';
				} else {
					output += tempNM.getBinary().charAt(i) == '.' ? '.' : '0';
				}
			} else {
				if (ip.getBinary().charAt(i) == '1' || tempNM.getBinary().charAt(i) == '1') {
					output += '1';
				} else {
					output += tempNM.getBinary().charAt(i) == '.' ? '.' : '0';
				}

			}
		}

		String[] tempOut = output.split("\\W");
		for (int i = 0; i < tempOut.length; i++) {
			tempOut[i] = Integer.parseInt(tempOut[i], 2) + "";
		}

		return new IP(String.join(".", tempOut), IP.IP_ADRESSE_DECIMAL);
	}

}
