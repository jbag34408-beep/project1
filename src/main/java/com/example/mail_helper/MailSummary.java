package com.example.mail_helper;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public record MailSummary(int number, String from, String subject, Date sentDate) {
    private static final DateTimeFormatter KOREAN_DATE =
        DateTimeFormatter.ofPattern("M월 d일 a h시", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"));

    // 화면용: "11월 24일 오후 2시"
    public String sentDateText() {
        return sentDate == null ? "" : KOREAN_DATE.format(sentDate.toInstant());
    }
}
