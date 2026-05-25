package com.soundboard.soundboard.unit.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.soundboard.soundboard.audit.AuditAction;
import com.soundboard.soundboard.audit.AuditLogger;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.security.MyUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestAuditLogger {

    private AuditLogger auditLogger;
    private ListAppender<ILoggingEvent> appender;
    private Logger auditLogbackLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();

        auditLogbackLogger = (Logger) LoggerFactory.getLogger("AUDIT");
        appender = new ListAppender<>();
        appender.start();
        auditLogbackLogger.addAppender(appender);
        auditLogbackLogger.setLevel(Level.WARN);
    }

    @AfterEach
    void tearDown() {
        auditLogbackLogger.detachAppender(appender);
    }

    @Test
    void log_withCaller_emitsWarnOnAuditLogger() {
        MyUserPrincipal caller = principal("alice", Role.ADMIN);

        auditLogger.log(AuditAction.USER_ACTIVE_TOGGLED, caller, "active=true for user 'bob'");

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(events.get(0).getFormattedMessage())
                .contains("action=USER_ACTIVE_TOGGLED")
                .contains("role='ADMIN'")
                .contains("actor='alice'")
                .contains("active=true for user 'bob'");
    }

    @Test
    void log_withoutCaller_emitsWarnOnAuditLogger() {
        auditLogger.log(AuditAction.BOOTSTRAP_SUPER_ADMIN_CREATED,
                "bootstrapped SUPER_ADMIN 'admin' mustChangePassword=true");

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(events.get(0).getFormattedMessage())
                .contains("action=BOOTSTRAP_SUPER_ADMIN_CREATED")
                .contains("bootstrapped SUPER_ADMIN 'admin'");
    }

    @Test
    void warn_emitsWarnOnAuditLogger() {
        auditLogger.warn(AuditAction.DISK_FILE_DELETE_FAILED, "soundId=abc error=permission denied");

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(events.get(0).getFormattedMessage())
                .contains("action=DISK_FILE_DELETE_FAILED")
                .contains("soundId=abc error=permission denied");
    }

    @Test
    void usesNamedLogger_AUDIT() {
        assertThat(auditLogbackLogger.getName()).isEqualTo("AUDIT");
    }

    @Test
    void multipleCalls_eachProduceOneEvent() {
        MyUserPrincipal caller = principal("sa", Role.SUPER_ADMIN);

        auditLogger.log(AuditAction.USER_CREATED, caller, "created user 'newbie' role=USER");
        auditLogger.log(AuditAction.USER_HARD_DELETED, caller, "deleted user 'olduser' and 3 sounds");

        assertThat(appender.list).hasSize(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains("USER_CREATED");
        assertThat(appender.list.get(1).getFormattedMessage()).contains("USER_HARD_DELETED");
    }

    private static MyUserPrincipal principal(String username, Role role) {
        Users user = Users.builder()
                .username(username)
                .role(role)
                .active(true)
                .createdAt(Instant.now())
                .build();
        return new MyUserPrincipal(user);
    }
}
