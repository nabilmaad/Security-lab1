/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import models.UserAccount;
 
public class MailHelper {
	public static void sendEmail(UserAccount user, String generatedPassword) throws RuntimeException {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
 
		Session session = Session.getInstance(props,
			new javax.mail.Authenticator() {
                                @Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("franck.nabil.csi4139@gmail.com","passForCSI4139course");
				}
			});
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("franck.nabil@csi4139.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(user.getUsername()));
			message.setSubject("CSI4139 password reset");
			message.setText("Dear " + user.getUsername() + "," +
					"\n\n Here is your new password: " + generatedPassword +
                                        "\n\n Best Regards," +
                                        "\n\n Nabil Maadarani - 6134578 | Franck Mamboue - 6175122");
 
			Transport.send(message);
 
			System.out.print("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}