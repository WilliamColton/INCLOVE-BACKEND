package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.email.EmailSendResponse;

public interface EmailService {

    EmailSendResponse sendEmail(String to, String subject, String text);

}
