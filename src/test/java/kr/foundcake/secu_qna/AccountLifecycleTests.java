package kr.foundcake.secu_qna;

import kr.foundcake.secu_qna.entity.Ticket;
import kr.foundcake.secu_qna.entity.TicketCategory;
import kr.foundcake.secu_qna.repository.TicketRepository;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import kr.foundcake.secu_qna.service.RegistrationService;
import kr.foundcake.secu_qna.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountLifecycleTests {
    @Autowired MockMvc mvc;
    @Autowired UserAccountRepository users;
    @Autowired TicketRepository tickets;
    @Autowired RegistrationService registration;
    @Autowired TicketService ticketService;
    @Autowired ApplicationEventPublisher events;

    @BeforeEach
    void clean() {
        tickets.deleteAll();
        users.deleteAll();
    }

    @Test
    void secondLoginIsRejectedWhileFirstSessionIsActive() throws Exception {
        registration.register("soloUser1", "strong-password");
        MockHttpSession first = login("soloUser1");
        mvc.perform(get("/tickets").session(first)).andExpect(status().isOk());

        mvc.perform(post("/login").with(csrf())
                .param("username", "soloUser1").param("password", "strong-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"));
        mvc.perform(get("/tickets").session(first)).andExpect(status().isOk());
        assertThat(users.findByUsername("soloUser1")).isPresent();
    }

    @Test
    void authenticatedSessionDestructionRemovesAccountAndTickets() throws Exception {
        registration.register("idleUser1", "strong-password");
        MockHttpSession session = login("idleUser1");
        Ticket ticket = ticketService.create("idleUser1", "Old ticket", "Details", TicketCategory.GENERAL);
        events.publishEvent(new HttpSessionDestroyedEvent(session));

        assertThat(users.findByUsername("idleUser1")).isEmpty();
        assertThat(tickets.existsById(ticket.getId())).isFalse();
        registration.register("idleUser1", "strong-password");
        assertThat(users.findByUsername("idleUser1")).isPresent();
    }

    @Test
    void registrationWithoutLoginSurvivesAnonymousSessionDestruction() {
        registration.register("unusedUser1", "strong-password");
        events.publishEvent(new HttpSessionDestroyedEvent(new MockHttpSession()));

        assertThat(users.findByUsername("unusedUser1")).isPresent();
    }

    private MockHttpSession login(String username) throws Exception {
        MvcResult result = mvc.perform(post("/login").with(csrf())
                .param("username", username).param("password", "strong-password"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

}
