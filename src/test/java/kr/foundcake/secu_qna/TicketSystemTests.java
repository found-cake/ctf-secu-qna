package kr.foundcake.secu_qna;

import kr.foundcake.secu_qna.entity.Ticket;
import kr.foundcake.secu_qna.entity.TicketCategory;
import kr.foundcake.secu_qna.entity.UserAccount;
import kr.foundcake.secu_qna.logging.TicketAuditLogger;
import kr.foundcake.secu_qna.repository.TicketRepository;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import kr.foundcake.secu_qna.service.RegistrationService;
import kr.foundcake.secu_qna.service.TicketService;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketSystemTests {
    @Autowired MockMvc mvc;
    @Autowired UserAccountRepository users;
    @Autowired TicketRepository tickets;
    @Autowired RegistrationService registration;
    @Autowired TicketService service;
    @Autowired TicketAuditLogger audit;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void clean() {
        tickets.deleteAll();
        users.deleteAll();
    }

    @Test
    void registrationValidatesUsernameUniquenessAndBcrypt() throws Exception {
        mvc.perform(post("/register").with(csrf()).param("username", "alice1").param("password", "strong-password"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
        UserAccount account = users.findByUsername("alice1").orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo("strong-password");
        assertThat(encoder.matches("strong-password", account.getPasswordHash())).isTrue();

        mvc.perform(post("/register").with(csrf()).param("username", "a' OR 1=1").param("password", "strong-password"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Username must")));
        mvc.perform(post("/register").with(csrf()).param("username", "alice1").param("password", "strong-password"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("already taken")));
        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void formLoginSucceedsAndFails() throws Exception {
        registration.register("alice1", "strong-password");
        mvc.perform(post("/login").with(csrf()).param("username", "alice1").param("password", "strong-password"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/tickets"));
        mvc.perform(post("/login").with(csrf()).param("username", "alice1").param("password", "wrong-password"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void ticketAccessIsAuthenticatedAndOwnerScoped() throws Exception {
        registration.register("alice1", "strong-password");
        registration.register("bob123", "strong-password");
        Ticket ticket = service.create("alice1", "Need help", "Details", TicketCategory.TECHNICAL);
        mvc.perform(get("/tickets")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/tickets/" + ticket.getId())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/tickets/" + ticket.getId()).with(user("alice1").roles("USER")))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Need help")));
        mvc.perform(get("/tickets/" + ticket.getId()).with(user("bob123").roles("USER")))
            .andExpect(status().isNotFound());
        mvc.perform(get("/tickets").with(user("bob123").roles("USER")))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Need help"))));
    }

    @Test
    void ticketCreationRequiresCsrfAndUsesAuditCore() throws Exception {
        registration.register("alice1", "strong-password");
        mvc.perform(post("/tickets").with(user("alice1").roles("USER"))
            .param("subject", "Billing question").param("content", "Details").param("category", "PAYMENT"))
            .andExpect(status().isForbidden());

        assertThat(LogManager.getContext(false)).isInstanceOf(LoggerContext.class);
        assertThat(org.apache.commons.logging.LogFactory.getFactory().getClass().getName())
            .isEqualTo("org.apache.commons.logging.impl.Slf4jLogFactory");
        assertThat(org.slf4j.LoggerFactory.getILoggerFactory().getClass().getName())
            .isEqualTo("ch.qos.logback.classic.LoggerContext");
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        LoggerConfig logger = context.getConfiguration().getLoggerConfig("ticket-audit");
        CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();
        AbstractAppender capture = new AbstractAppender("capture-test", null, null, true, null) {
            @Override public void append(LogEvent event) { events.add(event.getMessage().getFormattedMessage()); }
        };
        capture.start();
        logger.addAppender(capture, Level.INFO, null);
        try {
            mvc.perform(post("/tickets").with(user("alice1").roles("USER")).with(csrf())
                .param("subject", "Billing question").param("content", "Details").param("category", "PAYMENT"))
                .andExpect(status().is3xxRedirection());
            assertThat(events).anyMatch(event -> event.contains("user=alice1, subject=Billing question"));
            assertThat(tickets.count()).isEqualTo(1);
        } finally {
            logger.removeAppender("capture-test");
            capture.stop();
        }
    }

    @Test
    void ticketHtmlIsEscapedInDetail() throws Exception {
        registration.register("alice1", "strong-password");
        Ticket ticket = service.create("alice1", "<script>alert(1)</script>",
            "<img src=x onerror=alert(1)>", TicketCategory.GENERAL);
        mvc.perform(get("/tickets/" + ticket.getId()).with(user("alice1").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;img")))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>alert(1)</script>"))));
    }

    @Test
    void sessionNoticeAppearsOnEveryPage() throws Exception {
        registration.register("alice1", "strong-password");
        Ticket ticket = service.create("alice1", "Footer check", "Details", TicketCategory.GENERAL);
        String notice = "After sign-in, 10 minutes idle or signing out deletes your account and tickets.";

        mvc.perform(get("/login")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(notice)));
        mvc.perform(get("/register")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(notice)));
        mvc.perform(get("/account").with(user("alice1").roles("USER"))).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(notice)));
        mvc.perform(get("/tickets").with(user("alice1").roles("USER"))).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(notice)));
        mvc.perform(get("/tickets/new").with(user("alice1").roles("USER"))).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(notice)));
        mvc.perform(get("/tickets/" + ticket.getId()).with(user("alice1").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(notice)));
    }

    @Test
    void java17Log4jLookupTimesOutOnSlowLdap() throws Exception {
        registration.register("alice1", "strong-password");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            listener.setSoTimeout(5000);
            String subject = "${jndi:ldap://127.0.0.1:" + listener.getLocalPort() + "/probe}";
            Future<Integer> creation = executor.submit(() -> mvc.perform(post("/tickets")
                    .with(user("alice1").roles("USER")).with(csrf())
                    .param("subject", subject).param("content", "Details").param("category", "GENERAL"))
                .andReturn().getResponse().getStatus());
            try (Socket connection = listener.accept()) {
                assertThat(connection.getInetAddress().isLoopbackAddress()).isTrue();
                assertThat(creation.get(6, TimeUnit.SECONDS)).isEqualTo(302);
            }
            mvc.perform(get("/tickets").with(user("alice1").roles("USER")))
                .andExpect(status().isOk());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void slowLookupsCannotOccupyEveryRequestThread() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try (ServerSocket listener = new ServerSocket(0, 2, InetAddress.getLoopbackAddress())) {
            listener.setSoTimeout(5000);
            String subject = "${jndi:ldap://127.0.0.1:" + listener.getLocalPort() + "/slow}";
            Future<Boolean> first = executor.submit(() -> audit.tryLogCreated("alice1", 1L, subject));
            Future<Boolean> second = executor.submit(() -> audit.tryLogCreated("alice1", 2L, subject));
            try (Socket firstConnection = listener.accept(); Socket secondConnection = listener.accept()) {
                assertThat(audit.tryLogCreated("alice1", 3L, "normal")).isFalse();
                assertThat(first.get(6, TimeUnit.SECONDS)).isTrue();
                assertThat(second.get(6, TimeUnit.SECONDS)).isTrue();
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
