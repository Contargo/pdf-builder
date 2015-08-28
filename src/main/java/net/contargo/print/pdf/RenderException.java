package net.contargo.print.pdf;

/**
 * A general exception denoting that something went wrong during `rendering`. This is a one-for-all checked exception
 * that users of the library will receive, if something goes wrong, which can not be attributed to API-misuse.
 *
 * <p>The exception always provides the source, or originating, failure (most likely an {@link java.io.IOException}).
 * </p>
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public final class RenderException extends Exception {

    /**
     * Constructs a new exception with the given message and originating cause.
     *
     * @param  message  to clarify the context of this exception
     * @param  cause  of the exception being raised
     */
    public RenderException(String message, Throwable cause) {

        super(message, cause);
    }
}
