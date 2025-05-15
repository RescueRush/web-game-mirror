package lu.rescue_rush.game_backend.sb.component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import lu.rescue_rush.game_backend.db.tables.monitor.email.VerifiedEmailTable;
import lu.rescue_rush.game_backend.integrations.email.EmailSenderConfig;

@Service
public class EmailBounceWatcher {

	@Autowired
	private EmailSenderConfig emailConfig;

	@Autowired
	private VerifiedEmailTable VERIFIED_EMAIL;

	public void checkForBounces() throws MessagingException, IOException {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");

		Session session = Session.getInstance(props, null);
		Store store = session.getStore();

		store.connect("imap.gmail.com", emailConfig.username, emailConfig.password);

		Folder inbox = store.getFolder("MAIL-DELIVERY-SYSTEM");
		inbox.open(Folder.READ_ONLY);

		FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		Message[] messages = inbox.search(unseenFlagTerm);

		System.out.println(inbox);
		System.out.println(Arrays.toString(messages));
		for (Message message : messages) {
			Address[] froms = message.getFrom();
			System.out.println(message);
			System.out.println(Arrays.toString(froms));
			if (froms != null && froms[0].toString().toLowerCase().equals("mailer-daemon@googlemail.com")) {
				String subject = message.getSubject();
				Object content = message.getContent();

				System.out.println(subject + " = " + content.getClass().getName() + " (" + content + ")");

				if (content instanceof String && ((String) content).contains("550 5.1.1")) {
					System.out.println("Bounce detected!");
					System.out.println("Subject: " + subject);
					System.out.println("Content: " + content);
				} else if (content instanceof Multipart) {
					Multipart mp = (Multipart) content;
					for (int i = 0; i < mp.getCount(); i++) {
						BodyPart part = mp.getBodyPart(i);
						if (part.getContentType().toLowerCase().contains("text")) {
							String body = part.getContent().toString();
							if (body.contains("550 5.1.1")) {
								System.out.println("Bounce detected (multipart)!");
								System.out.println("Subject: " + subject);
								System.out.println("Content: " + body);
							}
						}
					}
				}
			}
		}

		inbox.close(false);
		store.close();
	}

}
