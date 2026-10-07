package kr.foundcake.secu_qna.config;

import kr.foundcake.secu_qna.service.AccountLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountSessionCleanupListener {
    private final AccountLifecycleService lifecycle;

    @EventListener
    public void onSessionDestroyed(HttpSessionDestroyedEvent event) {
        for (SecurityContext context : event.getSecurityContexts()) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                lifecycle.deleteAccount(authentication.getName());
            }
        }
    }
}
