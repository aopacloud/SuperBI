package net.aopacloud.superbi.common.core.exception.auth;

public class NoWorkspacePermissionException extends RuntimeException {
    public NoWorkspacePermissionException() {
        super();
    }

    public NoWorkspacePermissionException(String message) {
        super(message);
    }
}
