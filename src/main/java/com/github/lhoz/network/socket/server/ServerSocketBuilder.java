/**
 *
 */
package com.github.lhoz.network.socket.server;

/**
 * @author Lhoz
 *
 */
public class ServerSocketBuilder extends ServerSocket {
	/**
	 *
	 */
	public ServerSocketBuilder() {
	}

	/**
	 * @return
	 */
	public ServerSocket build() {
		return this;
	}

	/**
	 * @param autoReopen
	 * @return
	 */
	public ServerSocketBuilder withAutoReopen(final boolean autoReopen) {
		this.options.setAutoReopen(autoReopen);
		return this;
	}

	/**
	 * @param clients
	 * @return
	 */
	public ServerSocketBuilder withClients(final int clients) {
		if (clients > 0) {
			this.options.setClients(clients);
		}
		return this;
	}

	/**
	 * @param port
	 * @return
	 */
	public ServerSocketBuilder withPort(final int port) {
		if (port >= 0 && port <= 65535) {
			this.options.setPort(port);
		}
		return this;
	}

	/**
	 * @param readTries
	 * @return
	 */
	public ServerSocketBuilder withReadTries(final int readTries) {
		if (readTries > 0) {
			this.options.setReadTries(readTries);
		}
		return this;
	}

	/**
	 * @param timeout
	 * @return
	 */
	public ServerSocketBuilder withTimeout(final int timeout) {
		if (timeout > 0) {
			this.options.setTimeout(timeout);
		}
		return this;
	}

	/**
	 * @param writeTries
	 * @return
	 */
	public ServerSocketBuilder withWriteTries(final int writeTries) {
		if (writeTries > 0) {
			this.options.setWriteTries(writeTries);
		}
		return this;
	}
}
