/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
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
import models.UserAccount;
 
public class MailHelper {
	public static boolean sendEmail(UserAccount user, String receiver, String subject, String content, String[] attachments) throws RuntimeException {
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
                                    InternetAddress.parse(receiver));
                    message.setSubject(subject);
                    String body = "Dear " + user.getUsername() + ",\n\n ";
                    if ("CSI4139 password reset".equals(subject)) {
                        body += "Here is your new password: " + content +
                                "\n\n Best Regards," +
                                "\n\n Nabil Maadarani - 6134578 | Franck Mamboue - 6175122";
                    } else if ("CSI439 secure file".equals(subject)) {
                        body += content +
                                "\n\n Best Regards," +
                                "\n\n Franck Mamboue - 6175122";
                    }
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(body);
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart);
                    if(attachments != null) {
                        if (attachments.length > 0) {
                            for (String attachment : attachments) {
                                messageBodyPart = new MimeBodyPart();
                                String filename = attachment;
                                DataSource source = new FileDataSource(filename);
                                messageBodyPart.setDataHandler(new DataHandler(source));
                                messageBodyPart.setFileName(filename);
                                multipart.addBodyPart(messageBodyPart);
                            }
                        }
                    }

                    message.setContent(multipart);

                    // Send the complete message parts
                    message.setContent(multipart);
                    Transport.send(message);

                    System.out.print("Done");
                    return true;
		} catch (Exception e) {
			Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
                        return false;
		}
	}
}