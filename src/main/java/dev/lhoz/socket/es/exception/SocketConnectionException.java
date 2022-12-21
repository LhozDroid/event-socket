/**
 *
 */
package dev.lhoz.socket.es.exception;

/**
 * @author Lhoz
 *
 */
public class SocketConnectionException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @param cause
	 */
	public SocketConnectionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
