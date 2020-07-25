package de.epiceric.justmoney.exception;

public class NotEnoughMoneyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotEnoughMoneyException() {
        super();
    }

    public NotEnoughMoneyException(String message) {
        super(message);
    }

    public NotEnoughMoneyException(Throwable cause) {
        super(cause);
    }

    public NotEnoughMoneyException(String message, Throwable cause) {
        super(message, cause);
    }
}