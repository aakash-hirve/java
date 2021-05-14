package com.akash.cowin.common;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.akash.cowin.STATIC;

import io.vertx.core.json.JsonObject;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class EmailConfigs {
	
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	private static EmailConfigs emailConfigs = null;
	
	public static EmailConfigs getInstance() {
		if(emailConfigs == null) {
			emailConfigs = new EmailConfigs();
		}
		return emailConfigs;
	}

	private static final Properties prop = new Properties();
	private static Session session = null;

	static {
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");
		prop.put("mail.smtp.socketFactory.port", "465");
		prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	}

	/**
	 * Configure email settings
	 * 
	 * @param inputJson
	 * @return
	 */
	private static Session configureEmail(JsonObject inputJson) {

		try {
			if (session == null) {
				String username = inputJson.getJsonObject("email_settings").getString("username");
				String password = inputJson.getJsonObject("email_settings").getString("password");
//				System.out.println("configureEmail | Username: "+username+" Password: "+password);

				session = Session.getInstance(prop, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return session;
	}

	public static Session getEmailSession() {
		return configureEmail(STATIC.configJson);
	}

	/**
	 * 
	 * @param notificationMessage
	 * @param inputJson
	 */
	public void sendEmail(String notificationMessage, Session session) {
		try {
			
			Runnable runnableTask = () -> {
			    try {
			    	System.out.println(STATIC.requestId + " | sendEmail | Sending email...");
					String notifyTo = STATIC.configJson.getJsonObject("email_settings").getString("recipient_emails");
					Message message = new MimeMessage(session);
					message.setFrom(new InternetAddress("aakashhirve@gmail.com"));
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(notifyTo));
					message.setSubject("Cowin: Slot Availability Notification [" + Instant.now().toString() + "]");

					MimeBodyPart mimeBodyPart = new MimeBodyPart();
					mimeBodyPart.setContent(notificationMessage, "text/html");

					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(mimeBodyPart);
					message.setContent(multipart);
			    	
			    	System.out.println(STATIC.requestId + " | sendEmail | Sending message...");
			    	Transport.send(message);
			    	System.out.println(STATIC.requestId + " | sendEmail | Email notification sent!");
			    	
			    } catch (MessagingException me) {
					me.printStackTrace();
				}
			};
			
			// Using executor service with cached threadpool to avoid vertx threadblock
			executor.execute(runnableTask);			

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
