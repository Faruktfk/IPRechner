package ipRechner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Component;

public class IPRechnerFrame extends JFrame implements ActionListener {

	public static final int WIDTH = 600, HEIGHT = 530;
	private static final String IP_TF = "ipTF", NM_TF = "netzmaskeTF", INDICATOR = "!", ERWTRN = "erweitern",
			RECHTS_B = "rechtsBtn", LINKS_B = "linksBtn";

	private final String[] subLblNames = { "IPs in einem Netz", "Anzahl der Netze", "Anzahl der IPs", "Netzmaske",
			"Subnetze" }, normalLblNames = { "NetzID", "Broadcast", "Default-Gateway", "Anzahl g. IPs", "IP Range" };

	private JTextField[] textFields;
	private JLabel errorMsgLbl;
	private JButton rechnenBtn, loeschenBtn, btnLeft, btnRight, copyBtn;
	private IPRechner ipRechner;

	private JTextComponent[] e_textComponent;
	private ArrayList<Component> guiComponents = new ArrayList<>();
	private boolean isErweitert;
	private int erweiterung = 300;
	private int subnetIndex;

	public IPRechnerFrame() {
		isErweitert = false;
		setTitle("IP-Rechner");
		setSize(WIDTH, isErweitert ? HEIGHT + erweiterung : HEIGHT);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);

		loadPage();
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (e.getSource().equals(rechnenBtn)) {
			String ipErr = ipRechner.überprüfeEingabe(textFields[0].getText().strip().trim(), IP.IP_ADRESSE_DECIMAL);

			String nmErr = "";
			if (ipRechner.getNM() != null && ipRechner.getNM().equals(textFields[1].getText())) {
				nmErr = ipRechner.überprüfeEingabe(textFields[2].getText().strip().trim(), IP.NM_KURZ);
				nmErr = nmErr != null ? ipRechner.überprüfeEingabe(textFields[1].getText().strip().trim(), IP.NM_LANG)
						: nmErr;
			} else {
				nmErr = ipRechner.überprüfeEingabe(textFields[1].getText().strip().trim(), IP.NM_LANG);
				nmErr = nmErr != null ? ipRechner.überprüfeEingabe(textFields[2].getText().strip().trim(), IP.NM_KURZ)
						: nmErr;
			}

			String ErrMsg = ipErr != null ? ipErr : nmErr != null ? nmErr : "";
			errorMsgLbl.setText(ErrMsg);
			if (ErrMsg.length() == 0) {
				fillNormals();
			} else {
				if (!isErweitert) {
					for (int i = 3; i < textFields.length; i++) {
						textFields[i].setText("");
					}
				}
				ipRechner.resetAll();
			}
			textFields[0].requestFocusInWindow();

		} else if (e.getSource().equals(loeschenBtn)) {
			for (int i = 0; i < 3; i++) {
				textFields[i].setText("");
			}
			if (isErweitert) {
				for (int i = 0; i < e_textComponent.length; i++) {
					e_textComponent[i].setText("");
				}

			} else {
				for (int i = 3; i < textFields.length; i++) {
					textFields[i].setText("");
				}
			}

			errorMsgLbl.setText("");
			ipRechner.resetAll();
			textFields[0].requestFocusInWindow();

		} else if (e.getSource().equals(copyBtn)) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(gatherAllInfo()),
							null);
					copyBtn.setText("Kopiert");
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					copyBtn.setText("Kopieren");

				}
			}).start();

		} else if (command.equals(IP_TF)) {
			textFields[1].requestFocusInWindow();

		} else if (command.equals(NM_TF)) {
			textFields[0].requestFocusInWindow();
			rechnenBtn.doClick();

		} else if (command.equals(ERWTRN)) {
			this.setSize(WIDTH, isErweitert ? HEIGHT : HEIGHT + erweiterung);
			isErweitert = !isErweitert;
			for (Component c : guiComponents) {
				getContentPane().remove(c);
			}
			setLocationRelativeTo(null);
			if (isErweitert) {
				subnettingRechner();
				btnLeft.setEnabled(false);
				btnRight.setEnabled(false);

			} else {
				normalRechner();
			}

		} else if (command.equals(LINKS_B) || command.equals(RECHTS_B)) {
			boolean resetSubFields = false;
			if (command.equals(LINKS_B) && subnetIndex == 1) {
				((JButton) e.getSource()).setEnabled(false);
				resetSubFields = true;
			}

			if (command.equals(RECHTS_B) && subnetIndex == (32 - ipRechner.getNM().getNumberOf1()) - 2) {
				((JButton) e.getSource()).setEnabled(false);
				resetSubFields = true;
			}

			if (command.equals(RECHTS_B)) {
				btnLeft.setEnabled(true);
			} else {
				btnRight.setEnabled(true);
			}

			subnetIndex = command.equals(RECHTS_B) ? subnetIndex + 1 : subnetIndex - 1;

			String withIndicator = generateIPWIndicator(ipRechner.getNM().getNumberOf1());
			e_textComponent[0].setText(withIndicator);

			if (!resetSubFields) {
				fillSubnettings(calculateSubnetting(withIndicator));
			} else {
				for (int i = 1; i < e_textComponent.length; i++) {
					if (e_textComponent[i] != null) {
						e_textComponent[i].setText("");
					}
				}
			}

		}

	}

	private String gatherAllInfo() {
		String output = "-------------------------\n\n" + "IP-Adresse:\t" + textFields[0].getText() + "\n\nNetzmaske:\t"
				+ textFields[1].getText() + " / " + textFields[2].getText() + "\n\n";
		if (isErweitert) {
			output += e_textComponent[0].getText().replace(INDICATOR, "|") + "\n\n";
		}
		for (int i = 0; i < (isErweitert ? subLblNames.length : normalLblNames.length); i++) {
			output += (isErweitert
					? subLblNames[i] + ":\t" + (i == subLblNames.length - 1 ? "\n" : "")
							+ e_textComponent[i + 1].getText()
					: normalLblNames[i] + ":\t" + textFields[i + 3].getText()) + "\n\n";
		}

		return output;
	}

	private String generateIPWIndicator(int leftIndex) {
		String rawIP = String.join("", ipRechner.getNID().getBinary().split("\\D"));
		String output = "";
		int count = -1;
		for (int i = 0; i < rawIP.length(); i++) {
			if (Character.isDigit(rawIP.charAt(i))) {
				count++;
			}
			if (count == 8) {
				output += ".";
				count = 0;
			}
			if (i == leftIndex || i == leftIndex + subnetIndex) {
				output += INDICATOR;
			}
			output += rawIP.charAt(i);
		}

		return output;
	}

	private IPRechner[] calculateSubnetting(String lastState) {
		if (subnetIndex == 0) {
			return null;
		}
		int amount = (int) (Math.pow(2, subnetIndex));
		IPRechner[] subnets = new IPRechner[amount];
		lastState = String.join("", lastState.split("\\."));

		for (int i = 0; i < amount; i++) {
			subnets[i] = new IPRechner();
			String[] subs = lastState.split(INDICATOR);
			String subPart = Integer.toBinaryString(i);
			subPart = "0".repeat(subnetIndex - subPart.length()) + subPart;
			String tempSIP = subs[0] + subPart + subs[2];
			String sIP = "";
			int count = -1;
			for (int k = 0; k < tempSIP.length(); k++) {
				if (Character.isDigit(tempSIP.charAt(k))) {
					count++;
				}
				if (count == 8) {
					sIP += ".";
					count = 0;
				}
				sIP += tempSIP.charAt(k);
			}

			subnets[i].überprüfeEingabe(sIP, IP.IP_ADRESSE_BINARY);
			subnets[i].überprüfeEingabe(ipRechner.getNM().getNumberOf1() + subnetIndex + "", IP.NM_KURZ);

		}
		return subnets;
	}

	private void fillSubnettings(IPRechner[] subnets) {
		if (subnets == null) {
			return;
		}

		long ipIn1Net = Integer.parseInt(subnets[0].calculateOther()[5]);
		long nets = (long) Math.pow(2, subnetIndex);
		e_textComponent[1].setText(ipIn1Net + "");
		e_textComponent[2].setText(nets + "");
		e_textComponent[3].setText(ipIn1Net * nets + "");

		int numof1s = subnets[0].getNM().getNumberOf1();
		e_textComponent[4].setText(new IP(numof1s, IP.DONT_INVERT) + " / " + numof1s);

		String subns = "";
		for (int i = 0; i < subnets.length; i++) {
			String[] data = subnets[i].calculateOther();
			String subn = (i + 1) + "- " + "NetzID:\t" + data[2] + "\n" + "     BC:\t" + data[3] + "\n" + "     DG:\t"
					+ data[4] + "\n" + "     IP-Range:\t" + data[6] + "\n\n";
			subns += subn;

		}
		e_textComponent[5].setText(subns);
	}

	private void fillNormals() {
		String[] otherData = ipRechner.calculateOther();
		if (otherData == null) {
			return;
		}
		if (!isErweitert) {
			for (int i = 0; i < otherData.length; i++) {
				textFields[i + 1].setText(otherData[i]);
			}
		} else {
			for (int i = 0; i < e_textComponent.length; i++) {
				e_textComponent[i].setText("");
			}
			textFields[1].setText(otherData[0]);
			textFields[2].setText(otherData[1]);
			btnLeft.setEnabled(false);
			btnRight.setEnabled(true);
			subnetIndex = 0;
			e_textComponent[0].setText(generateIPWIndicator(ipRechner.getNM().getNumberOf1()));
		}

	}

	private void loadPage() {
		textFields = new JTextField[8];
		ipRechner = new IPRechner();

		JLabel lblNewLabel = new JLabel("/");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Cambria", Font.PLAIN, 23));
		lblNewLabel.setBounds(423, 94, 19, 41);
		getContentPane().add(lblNewLabel);

		JSeparator sp = new JSeparator(JSeparator.HORIZONTAL);
		sp.setBounds(100, 153, 400, 1);
		getContentPane().add(sp);

		errorMsgLbl = new JLabel();
		errorMsgLbl.setFont(new Font("Cambria", Font.PLAIN, 15));
		errorMsgLbl.setVerticalAlignment(SwingConstants.TOP);
		errorMsgLbl.setForeground(Color.RED);
		errorMsgLbl.setBounds(159, 18, 265, 38);
		getContentPane().add(errorMsgLbl);
		// --------------

		textFields[2] = new JTextField(2);
		textFields[2].addActionListener(this);
		textFields[2].setActionCommand(NM_TF);
		textFields[2].setFocusable(true);
		textFields[2].setFont(new Font("Cambria", Font.PLAIN, 18));
		textFields[2].setBounds(444, 100, 40, 31);
		getContentPane().add(textFields[2]);
		textFields[2].setColumns(10);

		String[] btnNms = { "RECHNEN", "LÖSCHEN" };
		String[] lbls = { "IP", "Netzmaske" };
		for (int i = 0; i < 2; i++) {
			JLabel lbl = new JLabel(lbls[i] + ":");
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setFont(new Font("Cambria", Font.PLAIN, 15));
			lbl.setBounds(27, 50 + i * 50, 112, 31);
			getContentPane().add(lbl);

			textFields[i] = new JTextField(15);
			textFields[i].addActionListener(this);
			textFields[i].setActionCommand(i == 0 ? IP_TF : NM_TF);
			textFields[i].setFocusable(true);
			textFields[i].setFont(new Font("Cambria", Font.PLAIN, 18));
			textFields[i].setBounds(159, 50 + i * 50, 265, 31);
			getContentPane().add(textFields[i]);

			JButton btn = new JButton(btnNms[i]);
			btn.addActionListener(this);
			btn.setFocusable(false);
			btn.setBackground(Color.LIGHT_GRAY);
			btn.setFont(new Font("Calibri", Font.BOLD, 15));
			if (i == 0) {
				rechnenBtn = btn;
			} else {
				loeschenBtn = btn;
			}
			getContentPane().add(btn);
		}

		copyBtn = new JButton("Kopieren");
		copyBtn.setFont(new Font("Calibri", Font.BOLD, 16));
		copyBtn.setFocusable(false);
		copyBtn.setBackground(new Color(59, 121, 227));
		copyBtn.setForeground(Color.white);
		copyBtn.addActionListener(this);
		getContentPane().add(copyBtn);

		JButton erweiternBtn = new JButton("+");
		erweiternBtn.setActionCommand(ERWTRN);
		erweiternBtn.addActionListener(this);
		erweiternBtn.setFont(new Font("Calibri", Font.BOLD, 13));
		erweiternBtn.setFocusable(false);
		erweiternBtn.setVerticalAlignment(SwingConstants.CENTER);
		erweiternBtn.setBackground(new Color(59, 121, 227));
		erweiternBtn.setForeground(Color.white);
		erweiternBtn.setBounds(510, 130, 40, 40);
		getContentPane().add(erweiternBtn);

		if (isErweitert) {
			subnettingRechner();
		} else {
			normalRechner();
		}
	}

	private void normalRechner() {

		for (int i = 3; i < textFields.length; i++) {
			JLabel lbl = new JLabel(normalLblNames[i - 3] + ":");
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setFont(new Font("Calibri", Font.PLAIN, 14));
			lbl.setOpaque(true);
			lbl.setBackground(new Color(203, 209, 212));
			lbl.setBounds(27, 175 + (i - 3) * 60, 112, 31);
			getContentPane().add(lbl);
			guiComponents.add(lbl);

			textFields[i] = new JTextField(15);
			textFields[i].setEditable(false);
			textFields[i].setOpaque(true);
			textFields[i].setFont(new Font("Cambria", Font.PLAIN, 18));
			textFields[i].setColumns(10);
			textFields[i].setBounds(159, 175 + (i - 3) * 60, 265, 31);
			getContentPane().add(textFields[i]);
			guiComponents.add(textFields[i]);
		}

		rechnenBtn.setBounds(444, 210, 125, 77);
		loeschenBtn.setBounds(444, 300, 125, 77);
		copyBtn.setBounds(458, 410, 100, 40);

	}

	private void subnettingRechner() {
		e_textComponent = new JTextComponent[subLblNames.length + 1];

		rechnenBtn.setBounds(470, 10, 100, 40);
		loeschenBtn.setBounds(470, 55, 100, 40);
		copyBtn.setBounds(33, 720, 100, 40);

		e_textComponent[0] = new JTextField();
		((JTextField) e_textComponent[0]).setHorizontalAlignment(SwingConstants.CENTER);
		e_textComponent[0].setEditable(false);
		e_textComponent[0].setBorder(null);
		e_textComponent[0].setBackground(new Color(217, 219, 222));
		e_textComponent[0].setOpaque(true);
		e_textComponent[0].setFont(new Font("Cambria", Font.BOLD, 16));
		e_textComponent[0].setBounds(124, 170, 350, 40);
		getContentPane().add(e_textComponent[0]);
		guiComponents.add(e_textComponent[0]);

		btnLeft = new JButton("<");
		btnLeft.setFont(new Font("Calibri", Font.BOLD, 20));
		btnLeft.setFocusable(false);
		btnLeft.setVerticalAlignment(SwingConstants.BOTTOM);
		btnLeft.setBackground(new Color(59, 121, 227));
		btnLeft.setForeground(Color.white);
		btnLeft.setBounds(250, 220, 45, 45);
		btnLeft.setEnabled(false);
		btnLeft.addActionListener(this);
		btnLeft.setActionCommand(LINKS_B);
		getContentPane().add(btnLeft);
		guiComponents.add(btnLeft);

		btnRight = new JButton(">");
		btnRight.setFont(new Font("Calibri", Font.BOLD, 20));
		btnRight.setFocusable(false);
		btnRight.setEnabled(false);
		btnRight.setVerticalAlignment(SwingConstants.BOTTOM);
		btnRight.setBackground(new Color(59, 121, 227));
		btnRight.setForeground(Color.white);
		btnRight.setBounds(300, 220, 45, 45);
		btnRight.addActionListener(this);
		btnRight.setActionCommand(RECHTS_B);
		getContentPane().add(btnRight);
		guiComponents.add(btnRight);

		for (int i = 0; i < subLblNames.length; i++) {
			int y = 290 + i * 50;

			JLabel lbl = new JLabel(subLblNames[i] + ":");
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setFont(new Font("Calibri", Font.PLAIN, 14));
			lbl.setBounds(30, y, 110, 40);
			lbl.setOpaque(true);
			lbl.setBackground(new Color(203, 209, 212));
			getContentPane().add(lbl);
			guiComponents.add(lbl);

			if (i != subLblNames.length - 1) {
				e_textComponent[i + 1] = new JTextField();
				((JTextField) e_textComponent[i + 1]).setHorizontalAlignment(SwingConstants.CENTER);
				e_textComponent[i + 1].setBounds(160, y, 390, 40);
				getContentPane().add(e_textComponent[i + 1]);
				guiComponents.add(e_textComponent[i + 1]);
			} else {
				e_textComponent[i + 1] = new JTextArea(11, 30);
				((JTextArea) e_textComponent[i + 1]).setLineWrap(true);
				((JTextArea) e_textComponent[i + 1]).setWrapStyleWord(true);
			}
			e_textComponent[i + 1].setEditable(false);
			e_textComponent[i + 1].setBorder(null);
			e_textComponent[i + 1].setBackground(new Color(217, 219, 222));
			e_textComponent[i + 1].setOpaque(true);
			e_textComponent[i + 1].setFont(new Font("Cambria", Font.PLAIN, 16));
		}

		JScrollPane scrollPane = new JScrollPane(e_textComponent[subLblNames.length],
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(160, 490, 390, 280);
		getContentPane().add(scrollPane);
		guiComponents.add(scrollPane);

	}

}
