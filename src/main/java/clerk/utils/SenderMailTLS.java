package clerk.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class SenderMailTLS {

    private Session session;

    public SenderMailTLS(String username, String password) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true");

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        session = Session.getDefaultInstance(props, auth);
    }

    public void sendFile(String from, String to, String subject, String file){
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart messageBodyPart;

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(file);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Sent!");

        } catch (MessagingException e) {
            //e.printStackTrace();
        }
    }

    public void sendMessage(String from, String to, String subject, String text){
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
            System.out.println("Sent!");

        } catch (MessagingException e) {
            //e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SenderMailTLS("sargisabrahamyan98@gmail.com", "")
                .sendMessage("sargisabrahamyan98@gmail.com", "sargisabrahamyan98@gmail.com",
                        "Temp Subject", "test text");
    }
}