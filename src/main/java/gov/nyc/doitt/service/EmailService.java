package gov.nyc.doitt.service;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


public class EmailService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JavaMailSender javaMailSender;
	@Value(value = "${emailFrom}")
	public String emailFrom;
	@Value(value = "${emailList}")
	public List<String> emailList;
	public void send(String messageText) {
        MimeMessage mail = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo((String[]) emailList.toArray());
            helper.setReplyTo(emailFrom);
            helper.setFrom(emailFrom);
            helper.setSubject("Bike Path Shapefile Import Process");
            helper.setText(messageText);
        } catch (MessagingException e) {
            e.printStackTrace();
			log.error(e.getLocalizedMessage());
        } finally {}
        javaMailSender.send(mail);
        //return helper;
    }
}
