/**
 *
 */
package dev.lhoz.socket.es.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import dev.lhoz.resilence.retry.RetryBuilder;
import dev.lhoz.socket.es.SocketOptions;
import dev.lhoz.socket.es.SocketReader;
import dev.lhoz.socket.es.SocketWriter;
import dev.lhoz.socket.es.exception.SocketConnectionException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ClientSocket {
	private final AtomicReference<ClientSocketStatus> status = new AtomicReference<>(ClientSocketStatus.DISCONNECTED);

	private Socket socket;
	private SocketWriter writer;
	private SocketReader reader;

	protected final SocketOptions options = new SocketOptions();

	private final ClientSocketObserver observer = new ClientSocketObserver();
	private final List<String> queue = Collections.synchronizedList(new ArrayList<String>());

	/**
	 *
	 */
	protected ClientSocket() {
	}

	/**
	 * @param listener
	 */
	public void add(final ClientSocketListener listener) {
		this.observer.add(listener);
	}

	/**
	 *
	 */
	public void clear() {
		this.observer.clear();
	}

	/**
	 * @return
	 */
	public ClientSocketStatus getStatus() {
		return this.status.get();
	}

	/**
	 * @param listener
	 */
	public void remove(final ClientSocketListener listener) {
		this.observer.remove(listener);
	}

	/**
	 * @param data
	 */
	public void send(final String data) {
		if (!StringUtils.isBlank(data)) {
			this.queue.add(data);
		}
	}

	/**
	 *
	 */
	public void start() {
		if (ClientSocketStatus.DISCONNECTED.equals(this.status.get())) {
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					this.observer.notifyOnStart();
					this.process();
				} catch (final Exception e) {
					ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
					ClientSocket.LOG.error(e.getLocalizedMessage());
				}
			});
		}
	}

	/**
	 *
	 */
	public void stop() {
		this.options.setAutoReconnect(false);
		this.status.set(ClientSocketStatus.DISCONNECTED);
		this.observer.notifyOnStop();
		this.clear();
	}

	/**
	 *
	 */
	private void close() {
		try {
			this.socket.close();
		} catch (final Exception e) {
			ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
		}
	}

	/**
	 *
	 */
	private void connect() {
		final AtomicReference<SocketConnectionException> exception = new AtomicReference<>();

		final RetryBuilder builder = new RetryBuilder()//
				.withSleep(this.options.getTimeout())//
				.withMethod(() -> {
					try {
						this.status.set(ClientSocketStatus.CONNECTING);
						this.observer.notifyOnConnecting();

						final String address = this.options.getAddress();
						final int port = this.options.getPort();

						final Socket socket = new Socket(address, port);
						socket.setSoTimeout(this.options.getTimeout());

						final SocketWriter writer = new SocketWriter(socket);
						final SocketReader reader = new SocketReader(socket);

						this.socket = socket;
						this.writer = writer;
						this.reader = reader;

						this.status.set(ClientSocketStatus.CONNECTED);
						this.observer.notifyOnConnected();
					} catch (final Exception e) {
						ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
						exception.set(new SocketConnectionException("Unable to connect.", e));
						this.observer.notifyOnConnectionFailed();
						throw new RuntimeException(e);
					}
				});

		if (!this.options.isAutoReconnect()) {
			builder.withAttempts(3);
		}

		try {
			builder.build().run();
		} catch (final Exception e) {
			ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
			ClientSocket.LOG.error(e.getLocalizedMessage());
			this.status.set(ClientSocketStatus.DISCONNECTED);
			this.observer.notifyOnDisconnected();
		}
	}

	/**
	 *
	 */
	private void process() {
		this.connect();

		while (ClientSocketStatus.CONNECTED.equals(this.status.get())) {
			this.read();
			this.write();
			this.sleep();
		}

		this.reader.close();
		this.writer.close();
		this.close();

		if (this.options.isAutoReconnect()) {
			this.sleep();
			this.process();
		}
	}

	/**
	 *
	 */
	private void read() {
		if (ClientSocketStatus.CONNECTED.equals(this.status.get())) {
			final AtomicReference<SocketConnectionException> exception = new AtomicReference<>();

			final RetryBuilder builder = new RetryBuilder()//
					.withSleep(this.options.getTimeout())//
					.withMethod(() -> {
						try {
							final String data = this.reader.read();
							if (!StringUtils.isEmpty(data)) {
								this.observer.notifyOnDataReceived(data);
							}
						} catch (final Exception e) {
							ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
							ClientSocket.LOG.error(e.getLocalizedMessage());
							exception.set(new SocketConnectionException("Disconnected from server.", e));
							throw new RuntimeException(e);
						}
					});

			if (this.options.getReadTries() > 0) {
				builder.withAttempts(this.options.getReadTries());
			}

			try {
				builder.build().run();
			} catch (final Exception e) {
				ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
				ClientSocket.LOG.error(e.getLocalizedMessage());
				this.status.set(ClientSocketStatus.DISCONNECTED);
				this.observer.notifyOnDisconnected();
			}
		}
	}

	/**
	 *
	 */
	private void sleep() {
		try {
			Thread.sleep(this.options.getTimeout());
		} catch (final Exception e) {
			ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
		}
	}

	/**
	 *
	 */
	private void write() {
		if (ClientSocketStatus.CONNECTED.equals(this.status.get())) {
			final AtomicReference<SocketConnectionException> exception = new AtomicReference<>();

			final RetryBuilder builder = new RetryBuilder()//
					.withSleep(this.options.getTimeout())//
					.withMethod(() -> {
						if (!this.queue.isEmpty()) {
							final List<String> pending = this.queue.stream() //
									.map(StringUtils::trim) //
									.collect(Collectors.toList());

							for (final String data : pending) {
								try {
									this.writer.write(data);
									this.queue.remove(0);

									this.observer.notifyOnDataSent(data);
								} catch (final Exception e) {
									ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
									ClientSocket.LOG.error(e.getLocalizedMessage());
									exception.set(new SocketConnectionException("Disconnected from server.", e));
									throw new RuntimeException(e);
								}
							}
						}
					});

			if (this.options.getWriteTries() > 0) {
				builder.withAttempts(this.options.getWriteTries());
			}

			try {
				builder.build().run();
			} catch (final Exception e) {
				ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
				ClientSocket.LOG.error(e.getLocalizedMessage());
				this.status.set(ClientSocketStatus.DISCONNECTED);
				this.observer.notifyOnDisconnected();
			}
		}
	}
}
