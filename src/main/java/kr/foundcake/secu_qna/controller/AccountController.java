package kr.foundcake.secu_qna.controller;

import kr.foundcake.secu_qna.entity.UserAccount;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import kr.foundcake.secu_qna.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final RegistrationService registration;
    private final UserAccountRepository users;

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/register")
    String register() { return "register"; }

    @PostMapping("/register")
    String register(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            registration.register(username, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            return "register";
        }
    }

    @GetMapping("/")
    String home() { return "redirect:/tickets"; }

    @GetMapping("/account")
    String account(Authentication authentication, Model model) {
        UserAccount account = users.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("account", account);
        return "account";
    }
}
