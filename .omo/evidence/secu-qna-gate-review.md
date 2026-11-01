# Defensive gate review

recommendation: APPROVE
blockers: []

## originalIntent
Build a Spring Boot 4.1.1 / Java 17 / MariaDB / Thymeleaf ticket application with BCrypt passwords, CSRF protection, owner-scoped tickets, and a deliberately vulnerable audit logging component. The assignment requests examination for additional unintended security defects.

## desiredOutcome
Registration, authentication, account display, ticket creation and private ticket viewing with the stated security boundaries.

## userOutcomeReview
No specific failure of the supplied application security criteria was identified in the inspected source or packaged resources. This is a defensive source/artifact review, not certification that no possible attack or flag-disclosure path exists. I did not execute or improve the deliberate exploit. No flag storage or deployment definition exists in the inspected application, so a claim that all flag paths are excluded is unverified.

## Findings (nonblocking notes, descending relevance)
1. Test evidence does not establish complete compromise or flag access: `src/test/java/kr/foundcake/secu_qna/TicketSystemTests.java:118` observes a socket connection and successful ticket response. Its scope must not be represented as command execution or flag retrieval. No such completion requirement was provided as a gate criterion.
2. `src/main/java/kr/foundcake/secu_qna/RegistrationService.java:32` translates every DataIntegrityViolationException to a duplicate username message. This can mask unrelated persistence integrity errors; no demonstrated security boundary violation.
3. `src/test/java/kr/foundcake/secu_qna/TicketSystemTests.java:51` combines registration, invalid username and duplicate handling. Lines 66 and 90 also combine behaviors. The empty context-load test in `SecuQnaApplicationTests.java:10` overlaps the integration suite's context initialization. These are maintenance/diagnostic notes, not stated-criterion violations.
4. Lombok declarations in `build.gradle.kts:35` are unused in inspected Java source. Low-impact unnecessary configuration.

## Direct security checks
- BCrypt configured in SecurityConfig.java:16; only hashes persisted, no password field rendered.
- SecurityConfig.java:34 permits only login, registration and CSS anonymously, retains default CSRF enforcement, and uses POST logout forms.
- TicketService.java:23 and :28 use owner-filtered repository methods. TicketController.java:36 derives owner from authenticated identity, not request input.
- Repositories use Spring Data derived queries; no concatenated SQL or custom native queries found.
- All template user values use escaped th:text/th:value; view names are fixed, with no user-controlled expression evaluation or raw HTML rendering found.
- Database credentials use environment placeholders; no embedded flag or application secret found in source/resources inspected.
- No actuator, H2 test database, or application test class found in production JAR inspection.
- Production resources match source bytes on final recheck; an initial application.yaml mismatch disappeared during a concurrent parent rebuild. Current DB_PASSWORD has no empty default. JAR includes requested Boot 4.1.1, MariaDB driver, Logback, and one Log4j API/core 2.14.1 pair, without log4j-to-slf4j.
- The compatibility provider is present in BOOT-INF/classes; source has narrow provider behavior, no input parsing, execution, or externally exposed API. Future dependency updates require revisiting this classpath override.

## Programming and remove-ai-slops pass
Consulted `/Users/cake/.codex/plugins/cache/sisyphuslabs/omo/5.1.19/skills/programming/SKILL.md` and `/Users/cake/.codex/plugins/cache/sisyphuslabs/omo/5.1.19/skills/remove-ai-slops/SKILL.md`. Java has no language-specific reference in the programming index, so shared criteria applied.

Directly checked excessive/useless tests, deletion-only tests, requested-removal assertions, tautologies, implementation mirroring, and unnecessary extraction/parsing/normalization. No deletion-only, requested-removal, self-derived expected-value or production normalization overfit found. Repository/service/controller seams serve framework or domain responsibilities. Validation is located at input boundaries. Tests use actual repositories and MVC rather than mocked service outputs. Logging assertions partly inspect implementation, but the named component and dependency selection are explicit requirements. Test bundling and redundant context-load coverage are notes above. No oversized source modules or speculative abstraction found.

No separate code-review report exists in the supplied evidence directory; this direct pass supplies skill-perspective coverage and does not treat that absence as a blocker.

## Checked artifact paths
- All 13 Java source files under `src/main/java/kr/foundcake/secu_qna/` and `src/main/java/org/springframework/boot/logging/log4j2/`.
- All six templates under `src/main/resources/templates/`, `static/css/app.css`, application.yaml, commons-logging.properties, log4j2.xml.
- Both Java test classes and `src/test/resources/application.yaml`.
- build.gradle.kts, settings.gradle.kts, gradle/wrapper/gradle-wrapper.properties, DESIGN.md.
- `build/libs/secu_qna-0.0.1-SNAPSHOT.jar`, including manifest, dependency inventory and source-resource byte comparison.
- `build/test-results/test/TEST-kr.foundcake.secu_qna.TicketSystemTests.xml` and `TEST-kr.foundcake.secu_qna.SecuQnaApplicationTests.xml`: recorded six tests, zero failures/errors. Results inspected, not rerun.

## Exact evidence gaps
- No full original brief or numbered success-criteria artifact; criteria reconstructed only from parent assignment.
- No git repository or diff baseline; reviewed current source in full.
- No separately persisted executor report, code-review report, manual QA matrix or notepad. Parent reports live MariaDB/HTTP smoke in its own tool transcript; this reviewer cannot independently inspect that transcript and does not count it as reproduced evidence.
- No current ulw-loop plan: status --json returned ULW_LOOP_PLAN_MISSING, so fallback report path used.
- No deployment/flag configuration to assess deployment-specific secret exposure.
- No new tests executed; intentional vulnerability reproduction outside this defensive review's verification scope.

Only this report was written. Application source and build configuration were not changed.

# Follow-up gate — 2026-10-07

recommendation: APPROVE
blockers: []

originalIntent: Retain Log4j 2.14.1, remove Java PoC artifacts so poc contains Python only, and review unintended security exposure.

desiredOutcome: Requested file cleanup with existing ticket security boundaries preserved; accurately bounded statements about container mitigations.

userOutcomeReview: `find poc -type f` returns only `poc/solve_dns.py`. Current JAR inventory retains log4j-api/core 2.14.1 and Logback, without H2 or actuator entries. No concrete additional application-level flag-disclosure or RCE path was demonstrated in this defensive review. APPROVE covers the requested cleanup and inspected defensive criteria; it does not certify DNS-only behavior or impossibility of RCE.

## Current notes, ordered by security relevance

- MEDIUM assurance limitation — docker-compose.yml:30: JVM reconstruction/code-loading controls are not outbound-network controls. Compose contains no DNS-only egress policy. Therefore LDAP/RMI communication must not be described as disabled by these flags. The recorded test at src/test/java/kr/foundcake/secu_qna/TicketSystemTests.java:141 exercises LDAP connection initiation, but it runs outside the Compose JVM configuration. RMI communication in the current container was not independently tested. This is the same intentional JNDI surface; no separate flag path or RCE bypass was proven.
- MEDIUM assurance limitation — Dockerfile:13 and docker-compose.yml:30: mitigation settings are attached by Compose, not embedded in the image launch command. Statements about mitigations apply to this Compose deployment, not arbitrary `docker run` or direct JAR launches. No alternate deployment was part of the supplied success criteria.
- INFO — docker-compose.yml:38: read-only root filesystem, tmpfs /tmp, non-root UID (Dockerfile:11), and no-new-privileges reduce consequences; they do not establish that code cannot execute or that process environment data cannot be read. FLAG remains in the app process environment by requirement (docker-compose.yml:29).
- INFO — the parent's report that a classic remote-codebase PoC produced no callback supports only that particular negative result. This reviewer did not inspect its transcript or independently reproduce it. No remaining RCE vector is asserted, and universal RCE prevention is not established.

Oracle JDK documentation describes trustSerialData as controlling LDAP object deserialization and factoriesFilter as controlling factory reconstruction, including the RMI provider. These properties do not constitute network isolation: https://docs.oracle.com/en/java/javase/17/docs/api/java.naming/module-summary.html . This distinction is the evidence-supported conclusion; no exploit construction or bypass testing was performed.

## Current defensive checks

- src/main/java/kr/foundcake/secu_qna/config/SecurityConfig.java:18 and :35: BCrypt and authenticated-by-default routing; CSRF remains enabled.
- src/main/java/kr/foundcake/secu_qna/service/TicketService.java:29 and :34: owner-scoped list and detail. Owner identity comes from Authentication in controller/TicketController.java:39.
- repository/TicketRepository.java:9 and repository/UserAccountRepository.java:8: derived parameterized persistence methods, no concatenated SQL found.
- All six templates use escaped th:text/th:value for user data, with fixed view names and no raw user HTML or user-selected template expressions found.
- No application endpoint reads FLAG, executes commands, handles uploads, proxies arbitrary URLs, or returns environment configuration in inspected Java sources.
- RegistrationService.java:34 now rethrows unrelated integrity errors; previous review note about blanket duplicate classification is resolved.
- The Boot compatibility shim remains a narrow PropertySource provider. No request-controlled values flow into it.

## Direct skill-perspective pass

Reused the programming and remove-ai-slops skills already consulted in this conversation. Directly rechecked current Java production and test code, configuration and requested cleanup for excessive/useless tests, deletion-only tests, requested-removal assertions, tautological expected values, implementation mirroring, and unnecessary extraction/parsing/normalization. No cleanup-specific tests were added, and none are needed to assert file deletion. Tests use real MVC/repositories, with behavior checks for access control, CSRF and HTML escaping. Provider-class assertions reflect the explicit logger-separation constraint. Bundled scenarios and the redundant context-load test remain nonblocking maintenance notes. No new production abstraction, parsing or normalization accompanies this deletion task. Python PoC implementation was not audited or improved; only its file inventory was verified.

The existing report above explicitly covers the same skill perspectives, though its old paths and assumptions are superseded by this dated follow-up. No separate current code-review report was supplied; the direct pass supplies current coverage.

## Current checked artifacts and gaps

Checked all current src/main/java files, all six templates, application.yaml, log4j2.xml, build.gradle.kts, Dockerfile, docker-compose.yml, current TicketSystemTests.java, poc file inventory, JAR dependency inventory, and both current XML test reports. XML reports dated 2026-10-07 record seven tests total with zero failures/errors; inspected, not independently rerun.

The exact original user transcript, numbered criteria, deleted-file diff, independently inspectable Docker QA transcript/matrix, current executor report and notepad path were not supplied. The parent reports fresh Docker HTTP checks and passing Gradle checks; those claims are not independently reproduced here. No active loop plan exists (status returned ULW_LOOP_PLAN_MISSING), hence the fallback report path. No concrete criterion failure was found; unproven assurance claims remain notes rather than invented blockers. Only this report was updated; application files were unchanged.
