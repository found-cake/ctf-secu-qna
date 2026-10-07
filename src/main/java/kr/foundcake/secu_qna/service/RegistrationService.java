package kr.foundcake.secu_qna.service;

import kr.foundcake.secu_qna.entity.UserAccount;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9]{4,20}");
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;

    public void register(String username, String password) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be 4–20 ASCII letters or digits.");
        }
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at least 8 characters and at most 72 UTF-8 bytes.");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        try {
            users.saveAndFlush(new UserAccount(username, passwords.encode(password)));
        } catch (DataIntegrityViolationException ex) {
            if (users.existsByUsername(username)) {
                throw new IllegalArgumentException("Username is already taken.", ex);
            }
            throw ex;
        }
    }
}
