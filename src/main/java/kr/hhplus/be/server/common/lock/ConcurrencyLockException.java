package kr.hhplus.be.server.common.lock;

public class ConcurrencyLockException extends RuntimeException{
    public ConcurrencyLockException(String message) {
        super(message);
    }

    public ConcurrencyLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
