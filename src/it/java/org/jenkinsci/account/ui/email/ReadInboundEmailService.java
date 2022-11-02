package org.jenkinsci.account.ui.email;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.mail.imap.IMAPFolder;

public class ReadInboundEmailService {
    private static final Logger logger = LoggerFactory.getLogger(ReadInboundEmailService.class);

    private final String host;
    private final int port;

    public ReadInboundEmailService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String retrieveEmail(String toAddressToSearchFor, String subjectToSearchFor, Date beginningOfTimeWindow) throws MessagingException, IOException {
        Session session = this.getImapSession();
        Store store = session.getStore("imap");
        store.connect(host, port, "", "");
        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        Message[] messages = inbox.getMessages();

        return Arrays.stream(messages)
                .filter(message -> {
                    try {
                        return ((InternetAddress)message.getRecipients(Message.RecipientType.TO)[0])
                                .getAddress()
                                .equals(toAddressToSearchFor)
                                &&
                                message.getSubject().equals(subjectToSearchFor)
                                &&
                                beginningOfTimeWindow.getTime() - 1000 < message.getReceivedDate().getTime();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(message -> {
                    try {
                        return String.valueOf(message.getContent());
                    } catch (IOException | MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }).findFirst()
                .orElse(null);
    }

    private Session getImapSession() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", String.valueOf(port));
        return Session.getDefaultInstance(props, null);
    }
}
