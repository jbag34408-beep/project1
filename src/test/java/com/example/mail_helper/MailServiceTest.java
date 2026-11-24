package com.example.mail_helper;

import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailServiceTest {
    private final Session session = Session.getInstance(new Properties());

    private static MimeBodyPart part(String text, String type) throws Exception {
        MimeBodyPart p = new MimeBodyPart();
        p.setContent(text, type + "; charset=UTF-8");
        return p;
    }

    // 실제 IMAP처럼 바이트로 썼다가 다시 읽어서 확인
    private MimeMessage roundTrip(MimeMultipart content) throws Exception {
        MimeMessage msg = new MimeMessage(session);
        msg.setContent(content);
        msg.saveChanges();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.writeTo(out);
        return new MimeMessage(session, new ByteArrayInputStream(out.toByteArray()));
    }

    @Test
    void nestedPlainTextWinsOverHtml() throws Exception {
        MimeMultipart alt = new MimeMultipart("alternative");
        alt.addBodyPart(part("<p>HTML 버전</p>", "text/html"));
        alt.addBodyPart(part("안녕하세요 아버지", "text/plain"));
        MimeBodyPart altPart = new MimeBodyPart();
        altPart.setContent(alt);
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(altPart);

        assertEquals("안녕하세요 아버지", MailService.bodyText(roundTrip(mixed)));
    }

    @Test
    void htmlOnlyMailBecomesTextAndAttachmentIsSkipped() throws Exception {
        MimeBodyPart attachment = part("첨부 내용", "text/plain");
        attachment.setDisposition(Part.ATTACHMENT);
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(attachment);
        mixed.addBodyPart(part("<html><body><h1>특가</h1><p>50% 할인</p></body></html>", "text/html"));

        assertEquals("특가 50% 할인", MailService.bodyText(roundTrip(mixed)));
    }

    @Test
    void dateIsEasyToRead() {
        Date d = Date.from(ZonedDateTime.of(2026, 11, 24, 14, 5, 0, 0, ZoneId.of("Asia/Seoul")).toInstant());
        assertEquals("11월 24일 오후 2시", new MailSummary(1, "a", "b", d).sentDateText());
    }
}
