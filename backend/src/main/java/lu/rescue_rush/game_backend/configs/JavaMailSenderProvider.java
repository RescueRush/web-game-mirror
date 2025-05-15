package lu.rescue_rush.game_backend.configs;

import java.util.HashMap;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.MessagingException;
import lu.rescue_rush.game_backend.integrations.email.EmailSenderConfig;

@Configuration
public class JavaMailSenderProvider {

	private final EmailSenderConfig emailSenderConfig;

	public JavaMailSenderProvider(EmailSenderConfig emailSenderConfig) {
		this.emailSenderConfig = emailSenderConfig;
	}

	@Bean
	public JavaMailSender javaMailSender() throws MessagingException {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(emailSenderConfig.host);
		mailSender.setPort(emailSenderConfig.port);

		mailSender.setUsername(emailSenderConfig.username);
		mailSender.setPassword(emailSenderConfig.password);
		mailSender.setProtocol(emailSenderConfig.protocol);

		Properties props = mailSender.getJavaMailProperties();
		HashMap<String, Object> properties = new HashMap<>();
		properties.put("mail.smtp.auth", emailSenderConfig.properties_mail_smtp_auth);
		properties.put("mail.smtp.starttls.enable", emailSenderConfig.properties_mail_smtp_starttls_enable);
		props.putAll(properties);
		mailSender.setJavaMailProperties(props);

		mailSender.testConnection();

		return mailSender;
	}
}
