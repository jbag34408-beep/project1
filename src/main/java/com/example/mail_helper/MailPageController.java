package com.example.mail_helper;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class MailPageController {
    private final MailService mailService;
    public MailPageController(MailService mailService) { this.mailService = mailService; }

    @GetMapping("/")
    public String list(Model model) throws MessagingException {
        model.addAttribute("mails", mailService.fetchRecent(10));
        return "list";
    }

    @GetMapping("/mail/{number}")
    public String detail(@PathVariable int number, Model model) throws MessagingException, IOException {
        model.addAttribute("mail", mailService.fetchBody(number));
        return "detail";
    }
}
