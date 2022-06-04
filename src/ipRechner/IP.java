package ipRechner;

public class IP {

	public static final int IP_ADRESSE_BINARY = -1, IP_ADRESSE_DECIMAL = 0, NM_LANG = 1, NM_KURZ = 2;
	public static final boolean INVERT = true, DONT_INVERT = false;

	private String[] decimal = new String[4], binary = new String[4];
	private boolean valid = true;
	private int numberOf1 = 0;

	public IP(int number, boolean invert) {
		if (number > 32 || number < 0) {
			valid = false;
			return;
		}
		this.numberOf1 = number;
		for (int i = 0; i < 4; i++) {
			String segment = "";
			for (int s = 0; s < 8; s++) {
				if (invert) {
					segment += number == 0 ? "1" : "0";
				} else {
					segment += number == 0 ? "0" : "1";
				}
				if (number > 0) {
					number--;
				}
			}
			binary[i] = segment;
			decimal[i] = Integer.parseInt(segment, 2) + "";
		}

	}

	public IP(String address, int eingabeTyp) {

		boolean zeroField = false;
		try {
			String[] addressSegments = address.split("\\W");
			if (eingabeTyp == IP_ADRESSE_BINARY) {
				binary = addressSegments;
			} else {
				decimal = addressSegments;
			}
			if (addressSegments.length != 4) {
				valid = false;
				return;
			}
			for (int i = 0; i < addressSegments.length; i++) {
				int segment = Integer.parseInt(
						eingabeTyp == IP_ADRESSE_BINARY ? Integer.parseInt(binary[i], 2) + "" : addressSegments[i]);
				if (segment > 255) {
					valid = false;
					return;
				}

				if (eingabeTyp == IP_ADRESSE_BINARY) {
					decimal[i] = segment+"";						
				} else {

					String binarySegment = Integer.toBinaryString(segment);
					String temp = "";
					for (int k = 0; k < 8 - binarySegment.length(); k++) {
						temp += "0";
					}
					binary[i] = temp + binarySegment;
					if (eingabeTyp == NM_LANG) {
						for (int j = 0; j < binary[i].length(); j++) {
							if (!zeroField && binary[i].charAt(j) == '0') {
								zeroField = true;
							}
							if (zeroField && binary[i].charAt(j) == '1') {
								valid = false;
								return;
							}
							if (binary[i].charAt(j) == '1') {
								numberOf1++;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			valid = false;
		}
	}

	public boolean getValid() {
		return valid;
	}

	public int getNumberOf1() {
		return numberOf1;
	}

	public String getDecimal() {
		return valid ? String.join(".", decimal) : null;
	}

	public String getBinary() {
		return valid ? String.join(".", binary) : null;
	}

	public IP getNextIP(boolean isPlus) {
		int lastSegment = Integer.parseInt(decimal[3]);
		lastSegment += isPlus ? 1 : -1;
		if (lastSegment > 255 || lastSegment < 0) {
			return null;
		}
		return new IP(String.join(".", decimal[0], decimal[1], decimal[2], lastSegment + ""), IP_ADRESSE_DECIMAL);

	}

	@Override
	public String toString() {
		return this.getDecimal();
	}

}
