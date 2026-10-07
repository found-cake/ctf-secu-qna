package kr.foundcake.secu_qna.logging;

import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TicketAuditLogger {
    private static final Logger AUDIT = LogManager.getLogger("ticket-audit");
    private final Semaphore slots = new Semaphore(2);

    public boolean tryLogCreated(String username, Long ticketId, String subject) {
        if (!slots.tryAcquire()) {
            return false;
        }
        try {
            AUDIT.info("Ticket created: id=" + ticketId + ", user=" + username + ", subject=" + subject);
            return true;
        } finally {
            slots.release();
        }
    }
}
