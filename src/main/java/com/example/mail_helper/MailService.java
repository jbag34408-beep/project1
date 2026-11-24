package com.example.mail_helper;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class MailService {
    private final MailProperties props;
    public MailService(MailProperties props) { this.props = props; }

    public List<MailSummary> fetchRecent(int limit) throws MessagingException {
        Properties p = new Properties();
        p.put("mail.store.protocol", "imaps");
        Session session = Session.getInstance(p);

        try (Store store = session.getStore("imaps")) {
            store.connect(props.host(), props.port(), props.username(), props.password());
            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_ONLY);        // 읽기 전용: 메일을 바꾸지 않음
                int total = inbox.getMessageCount();
                int start = Math.max(1, total - limit + 1);
                Message[] messages = inbox.getMessages(start, total);

                List<MailSummary> result = new ArrayList<>();
                for (int i = messages.length - 1; i >= 0; i--) {   // 최신 메일부터
                    Message m = messages[i];
                    result.add(new MailSummary(
                        m.getMessageNumber(), senderName(m), m.getSubject(), m.getSentDate()));
                }
                return result;
            }
        }
    }

    public MailDetail fetchBody(int number) throws MessagingException, IOException {
        Properties p = new Properties();
        p.put("mail.store.protocol", "imaps");
        Session session = Session.getInstance(p);

        try (Store store = session.getStore("imaps")) {
            store.connect(props.host(), props.port(), props.username(), props.password());
            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_ONLY);
                if (number < 1 || number > inbox.getMessageCount()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "메일을 찾을 수 없습니다");
                }
                Message m = inbox.getMessage(number);
                MailSummary summary = new MailSummary(number, senderName(m), m.getSubject(), m.getSentDate());
                return new MailDetail(summary, bodyText(m));   // 본문은 폴더가 열려 있을 때 읽어야 함
            }
        }
    }

    // text/plain이 있으면 그것, 없으면 text/html에서 글자만 뽑는다
    static String bodyText(Part message) throws MessagingException, IOException {
        Part plain = findPart(message, "text/plain");
        if (plain != null) return (String) plain.getContent();
        Part html = findPart(message, "text/html");
        if (html != null) return Jsoup.parse((String) html.getContent()).text();
        return "(본문이 없습니다)";
    }

    // 조각 안에 조각이 있을 수 있어 재귀로 찾는다. 첨부파일은 본문이 아니므로 건너뛴다.
    private static Part findPart(Part part, String mimeType) throws MessagingException, IOException {
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) return null;
        if (part.isMimeType(mimeType)) return part;
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                Part found = findPart(mp.getBodyPart(i), mimeType);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String senderName(Message m) throws MessagingException {
        Address[] from = m.getFrom();
        if (from == null || from.length == 0) return "(알 수 없음)";
        InternetAddress addr = (InternetAddress) from[0];
        return addr.getPersonal() != null ? addr.getPersonal() : addr.getAddress();
    }
}