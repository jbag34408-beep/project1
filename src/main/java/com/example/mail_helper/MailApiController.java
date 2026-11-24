package com.example.mail_helper;

import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MailApiController {
    private final MailService mailService;
    public MailApiController(MailService mailService) { this.mailService = mailService; }

    @GetMapping("/api/mails")
    public List<MailSummary> mails() throws MessagingException {
        return mailService.fetchRecent(10);
    }
}