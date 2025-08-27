package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import forfun.williamcolton.c.inclove.dto.email.resp.EmailSendResponse;
import forfun.williamcolton.c.inclove.exception.AuthErrorCode;
import forfun.williamcolton.c.inclove.exception.BusinessException;
import forfun.williamcolton.c.inclove.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class RecendEmailServiceImpl implements EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    @Override
    public EmailSendResponse sendEmail(String to, String subject, String text) {
        var resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .text(text)
                .subject(subject)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            return new EmailSendResponse(data.getId(), true, LocalDateTime.now());
        } catch (ResendException e) {
            log.info("email send fail: {}", e.toString());
            throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAIL);
        }
    }
}
