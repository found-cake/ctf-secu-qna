package kr.foundcake.secu_qna.controller;

import kr.foundcake.secu_qna.entity.Ticket;
import kr.foundcake.secu_qna.entity.TicketCategory;
import kr.foundcake.secu_qna.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService service;

    @GetMapping
    String list(Authentication authentication, Model model) {
        model.addAttribute("tickets", service.list(authentication.getName()));
        model.addAttribute("username", authentication.getName());
        return "tickets/list";
    }

    @GetMapping("/new")
    String newTicket(Model model) {
        model.addAttribute("categories", TicketCategory.values());
        return "tickets/new";
    }

    @PostMapping
    String create(Authentication authentication, @RequestParam String subject,
                  @RequestParam String content, @RequestParam TicketCategory category, Model model) {
        try {
            Ticket ticket = service.create(authentication.getName(), subject, content, category);
            return "redirect:/tickets/" + ticket.getId();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("subject", subject);
            model.addAttribute("content", content);
            model.addAttribute("category", category);
            model.addAttribute("categories", TicketCategory.values());
            return "tickets/new";
        }
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("ticket", service.get(id, authentication.getName()));
        return "tickets/detail";
    }
}
