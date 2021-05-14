package com.akash.cowin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.akash.cowin.InitiateCowin;
import com.akash.cowin.common.CowinJwt;

/**
 * 
 * @author Aakash Hirve
 * This clumsy UI is no longer required as OTP is received from Android phone directly to our HttpServer
 *
 */
public class OtpInput extends JFrame {

	/**
	 * Create a UI for user to enter their OTP
	 */
	private static final long serialVersionUID = 1L;

	static JFrame frame = new JFrame("Enter OTP");

	public OtpInput(String requestId, String txnId) {

		JLabel otpLabel = new JLabel();
		otpLabel.setText("Enter OTP");
		otpLabel.setBounds(90, 10, 60, 40);
		add(otpLabel);

		JTextArea textArea = new JTextArea();// create button
		textArea.setBounds(40, 50, 150, 20);
		System.out.println(requestId + " | JFrame | OTP give as: " +textArea.getText());
		add(textArea);

		JButton submit = new JButton();
		submit.setText("Submit");
		submit.setBounds(40, 90, 150, 20);

		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textArea.getText().isEmpty()) {
					
					// Pop up another input asking after how long to check for OTP
					dispose();
					JFrame otpPollerFrame = new JFrame("");
					
					JLabel newOtpLabel = new JLabel();
					newOtpLabel.setText("Specify OTP generation time(ms)");
					newOtpLabel.setBounds(40, 10, 180, 40);
					otpPollerFrame.add(newOtpLabel);

					JTextArea newTextArea = new JTextArea();// create button
					newTextArea.setBounds(40, 50, 150, 20);
					otpPollerFrame.add(newTextArea);
					
					JButton newSubmit = new JButton();
					newSubmit.setText("Submit");
					newSubmit.setBounds(40, 90, 150, 20);
					

					otpPollerFrame.add(newSubmit);
					otpPollerFrame.setSize(250, 180);
					otpPollerFrame.setLayout(null);
					otpPollerFrame.setVisible(true);
					
					
					// Inner listener for setting timer for OTP/token generation, in case OTP is blank
					newSubmit.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							dispose();
							otpPollerFrame.dispose();
							// Generate mobile OTP after user specified duration
							long otpRetryMs = Long.parseLong(newTextArea.getText());
							System.out.println(requestId + " | Second JFrame | Will send OTP after "+newTextArea.getText()+"ms i.e. "+((otpRetryMs/1000)/60)+" minute(s)...");
							
							InitiateCowin.getVertx().setTimer(otpRetryMs, handleJwtGeneration -> {
								CowinJwt.sendMobileOtp();
								
							});
						}
					});
					
				} else {
					System.out.println(requestId + " | JFrame | Sending back OTP: " + textArea.getText());
					CowinJwt.validateOtp(txnId, textArea.getText());
					dispose();
				}
			}
		});
		
		add(submit);
		setSize(250, 180);
		setLayout(null);
		setVisible(true);
	}
}
