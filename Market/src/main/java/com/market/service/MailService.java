package com.market.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.nio.file.Files;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMailWithAttachment(String to, String subject, String body) {
        MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setFrom(new InternetAddress("tututu@abv.com"));
            mimeMessage.setSubject(subject);
            mimeMessage.setText("a");


            File file = new File("C:/JAVA/03_DataBase_SpringBoot/SpringBoot/PDF/invoice.pdf");
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setText("", true);
            helper.addAttachment("Attachment File name", new ByteArrayResource(Files.readAllBytes(file.toPath())), "application/pdf");
        };

        try {
            mailSender.send(preparator);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }


}
