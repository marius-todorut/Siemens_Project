package ro.marius.train;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService{

    public void sendMail(String to,String subject,String text) {
        try {
            var props= new Properties();

            props.put("mail.smtp.host",Database.getProperty("mail.host"));
            props.put("mail.smtp.port",Database.getProperty("mail.port"));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable","true");

            var username=Database.getProperty("mail.username");
            var password=Database.getProperty("mail.password");
            var session=Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            var message=new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

            System.out.println("Email sent to " + to);
        }catch(Exception e) {
            System.out.println("Email could not be sent: " + e.getMessage());
        }
    }
}