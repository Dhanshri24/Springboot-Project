package com.ecom.util;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommanUtil {
	
	@Autowired
	private   JavaMailSender mailSender;
	
	public  Boolean sendMail(String url ,String reciepentEmail) throws UnsupportedEncodingException, MessagingException
	{
		MimeMessage message=mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(message);
		
		helper.setFrom("vaibhavharal11@gmail.com", "Shopping Cart");
		helper.setTo(reciepentEmail);
		
		String content="<p>Hello</p>"+"<p>You Requested To Reset Your Password</p>"+
		"<p>Hello</p>Click the link below to change your password"+"<p><a href=\""+url+"\">Change My Password</p>";
		
		helper.setSubject("Password Reset");
		helper.setText(content,true);
		mailSender.send(message);
		
		
		
		return true;
		
	}

	public static String generateUrl(HttpServletRequest request) {
		
		String siteUrl=request.getRequestURL().toString();
		
		
		
		return  siteUrl.replace(request.getServletPath(), "");
	}

}
