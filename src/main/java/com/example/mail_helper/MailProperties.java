package com.example.mail_helper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail.imap")
public record MailProperties(String host, int port, String username, String password) {}
