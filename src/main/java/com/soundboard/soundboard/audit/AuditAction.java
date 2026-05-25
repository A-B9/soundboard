package com.soundboard.soundboard.audit;

public enum AuditAction {
    BOOTSTRAP_SUPER_ADMIN_CREATED,
    USER_ACTIVE_TOGGLED,
    USER_MUST_CHANGE_PASSWORD_SET,
    USER_CREATED,
    USER_HARD_DELETED,
    DISK_FILE_DELETE_FAILED
}
