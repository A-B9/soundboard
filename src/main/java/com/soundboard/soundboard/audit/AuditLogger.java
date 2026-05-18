package com.soundboard.soundboard.audit;

import com.soundboard.soundboard.security.MyUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Log a user-triggered audit event with caller context.
     */
    public void log(AuditAction action, MyUserPrincipal caller, String details) {
        AUDIT.warn("action={} role='{}' actor='{}' {}",
                action, caller.getRole(), caller.getUsername(), details);
    }

    /**
     * Log a system-triggered audit event (no user caller, e.g. bootstrap).
     */
    public void log(AuditAction action, String details) {
        AUDIT.warn("action={} {}", action, details);
    }

    /**
     * Log an audit warning (e.g. best-effort cleanup failure).
     */
    public void warn(AuditAction action, String details) {
        AUDIT.warn("action={} {}", action, details);
    }
}
