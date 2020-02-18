package com.ml.utils;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class GoogleMailSenderUtil {

    static final String username = "beatrizdassieu@gmail.com";
    static final String password = "MePica33";
    //static final String adressList = "sebamuzzu2@gmail.com, to_username_b@yahoo.com";
    static final String adressList = "sebamuzzu2@gmail.com";

    public static void sendMail(String subject, String text){
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("fromasdasdasdasd@gmail.com"));
            message.setRecipients(
                    MimeMessage.RecipientType.TO,
                    InternetAddress.parse(adressList)
            );
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        sendMail("vendiste papa 2!","Dear Mail Crawler,\n\n Please do not spam my email!");
    }

}
