package kr.foundcake.secu_qna.service;

import java.util.List;
import kr.foundcake.secu_qna.repository.TicketRepository;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountLifecycleService {
    private final UserAccountRepository users;
    private final TicketRepository tickets;

    @Transactional
    public void deleteAccount(String username) {
        users.findByUsernameForUpdate(username).ifPresent(account -> {
            tickets.deleteByOwnerIds(List.of(account.getId()));
            users.delete(account);
        });
    }
}
