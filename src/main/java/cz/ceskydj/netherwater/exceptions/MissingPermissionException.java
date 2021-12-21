package cz.ceskydj.netherwater.exceptions;

@SuppressWarnings("unused")
public class MissingPermissionException extends Exception {
    public MissingPermissionException() {}

    public MissingPermissionException(String message) {
        super(message);
    }

    public MissingPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingPermissionException(Throwable cause) {
        super(cause);
    }

    public MissingPermissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
