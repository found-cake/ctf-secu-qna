package kr.foundcake.secu_qna.service;

import kr.foundcake.secu_qna.entity.Ticket;
import kr.foundcake.secu_qna.entity.TicketCategory;
import kr.foundcake.secu_qna.entity.UserAccount;
import kr.foundcake.secu_qna.logging.TicketAuditLogger;
import kr.foundcake.secu_qna.repository.TicketRepository;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository tickets;
    private final UserAccountRepository users;
    private final TicketAuditLogger audit;

    @Transactional(readOnly = true)
    public List<Ticket> list(String username) {
        return tickets.findByOwnerUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional(readOnly = true)
    public Ticket get(Long id, String username) {
        return tickets.findByIdAndOwnerUsername(id, username)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Transactional
    public Ticket create(String username, String subject, String content, TicketCategory category) {
        if (subject == null || subject.isBlank() || subject.length() > 160) {
            throw new IllegalArgumentException("Subject must be 1–160 characters.");
        }
        if (content == null || content.isBlank() || content.length() > 10000) {
            throw new IllegalArgumentException("Content must be 1–10000 characters.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Choose a category.");
        }
        UserAccount owner = users.findByUsername(username).orElseThrow();
        Ticket ticket = tickets.saveAndFlush(new Ticket(owner, subject, content, category));
        if (!audit.tryLogCreated(username, ticket.getId(), subject)) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS);
        }
        return ticket;
    }
}
