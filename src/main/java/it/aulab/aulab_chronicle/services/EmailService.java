package it.aulab.aulab_chronicle.services;

public interface EmailService {

    void sendSimpleEmail(String to, String subject, String text);

}
