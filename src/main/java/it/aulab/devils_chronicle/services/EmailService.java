package it.aulab.devils_chronicle.services;

public interface EmailService {

    void sendSimpleEmail(String to, String subject, String text);

}
