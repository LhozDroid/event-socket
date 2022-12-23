/**
 *
 */
package dev.lhoz.network.es.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import dev.lhoz.resilence.retry.RetryBuilder;
import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ClientSocket {
	protected final AtomicReference<ClientSocketStatus> status = new AtomicReference<>(ClientSocketStatus.DISCONNECTED);
	private ExecutorService executor;

	protected Socket socket;
	private ClientSocketWriter writer;
	private ClientSocketReader reader;

	protected final ClientSocketOptions options = new ClientSocketOptions();

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
	public String getAddress() {
		return this.socket.getInetAddress().getHostAddress();
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
		if (this.executor == null) {
			this.executor = Executors.newSingleThreadExecutor();
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

		if (!ClientSocketStatus.DISCONNECTED.equals(this.status.get())) {
			this.status.set(ClientSocketStatus.DISCONNECTED);
			this.observer.notifyOnStop();
		}

		this.clear();

		this.executor.shutdown();
		try {
			this.executor.awaitTermination(this.options.getTimeout(), TimeUnit.MILLISECONDS);
		} catch (final Exception e) {
			ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
		}
		this.executor = null;
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
		if (ClientSocketStatus.DISCONNECTED.equals(this.status.get()) && this.socket == null) {
			this.connectFull();
		} else {
			this.connectPartial();
		}
	}

	/**
	 * Attempts to connect by creating the connection itself
	 */
	private void connectFull() {
		final AtomicReference<Exception> exception = new AtomicReference<>();

		final RetryBuilder builder = new RetryBuilder()//
				.withSleep(this.options.getTimeout())//
				.withMethod(() -> {
					try {
						this.status.set(ClientSocketStatus.CONNECTING);
						this.observer.notifyOnConnecting();

						this.createSocket();
						this.socket.setSoTimeout(this.options.getTimeout());

						this.createReaderWriter();

						this.status.set(ClientSocketStatus.CONNECTED);
						this.observer.notifyOnConnected();
					} catch (final Exception e) {
						ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
						exception.set(new Exception("Unable to connect.", e));
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
	 * Attempts to create the reader and writer only
	 */
	private void connectPartial() {
		final AtomicReference<Exception> exception = new AtomicReference<>();

		final RetryBuilder builder = new RetryBuilder()//
				.withSleep(this.options.getTimeout())//
				.withMethod(() -> {
					try {
						this.options.setAddress(this.socket.getInetAddress().getHostAddress());
						this.options.setPort(this.socket.getPort());

						this.socket.setSoTimeout(this.options.getTimeout());

						this.createReaderWriter();

						this.status.set(ClientSocketStatus.CONNECTED);
						this.observer.notifyOnConnected();
					} catch (final Exception e) {
						ClientSocket.LOG.debug(e.getLocalizedMessage(), e);
						exception.set(new Exception("Unable to connect.", e));
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
	 * @throws Exception
	 */
	private void createReaderWriter() throws Exception {
		final ClientSocketWriter writer = new ClientSocketWriter(this.socket);
		final ClientSocketReader reader = new ClientSocketReader(this.socket);

		this.writer = writer;
		this.reader = reader;
	}

	/**
	 * @throws Exception
	 */
	private void createSocket() throws Exception {
		final String address = this.options.getAddress();
		final int port = this.options.getPort();

		final Socket socket = new Socket(address, port);
		this.socket = socket;
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
		} else {
			this.stop();
		}
	}

	/**
	 *
	 */
	private void read() {
		if (ClientSocketStatus.CONNECTED.equals(this.status.get())) {
			final AtomicReference<Exception> exception = new AtomicReference<>();

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
							exception.set(new Exception("Disconnected from server.", e));
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
			final AtomicReference<Exception> exception = new AtomicReference<>();

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
									exception.set(new Exception("Disconnected from server.", e));
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
