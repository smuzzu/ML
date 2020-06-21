package com.ml.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class GoogleMailSenderUtil {

    static final String username = "beatrizdassieu@gmail.com";
    static final String password = "MePica33";
    //static final String adressList = "sebamuzzu2@gmail.com, to_username_b@yahoo.com";
    static final String adressList = "sebamuzzu2@gmail.com";

    public static boolean sendMail(String subject, String text,String destinationAddress, String []attachmets){
        if (destinationAddress==null || destinationAddress.isEmpty()){
            destinationAddress=adressList;
        }
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
            try {
                message.setFrom(new InternetAddress("beatrizdassieu@gmail.com","Galperin"));
            } catch (UnsupportedEncodingException e) {
                String errorMsg="UnsupportedEncodingException sending email=" + subject;
                System.out.println(errorMsg);
                Logger.log(errorMsg);
                Logger.log(e);
                e.printStackTrace();
            }
            message.setRecipients(
                    MimeMessage.RecipientType.TO,
                    InternetAddress.parse(destinationAddress)
            );
            message.setSubject(subject);

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // body
            BodyPart messageBodyPart = new MimeBodyPart();
            //messageBodyPart.setText(text);
            messageBodyPart.setContent(text,"text/html");
            multipart.addBodyPart(messageBodyPart);

            if (attachmets!=null){
                for (int i=0; i<attachmets.length; i++){
                    String attachment=attachmets[i];
                    if (attachment!=null && !attachment.isEmpty()){
                        messageBodyPart = new MimeBodyPart();
                        DataSource source = new FileDataSource(attachment);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(attachment);
                        multipart.addBodyPart(messageBodyPart);
                    }
                }
            }

            // Send the complete message parts
            message.setContent(multipart);

            Transport.send(message);


        } catch (MessagingException e) {
            String errorMsg="exception sending email=" + subject;
            System.out.println(errorMsg);
            Logger.log(errorMsg);
            Logger.log(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void sendMail(String subject, String text, String destinationAddress){
        sendMail(subject,text,destinationAddress,null);
    }


    public static void main(String[] args) {
        sendMail("vendiste papa 2!","Dear Mail Crawler,\n\n Please do not spam my email!",null);
    }

}
