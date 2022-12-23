/**
 *
 */
package com.github.lhoz.network.socket.server;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;

import com.github.lhoz.network.socket.client.ClientSocket;
import com.github.lhoz.network.socket.client.ClientSocketBuilder;
import com.github.lhoz.resilence.retry.RetryBuilder;

import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ServerSocket {
	private final AtomicReference<ServerSocketStatus> status = new AtomicReference<>(ServerSocketStatus.DISCONNECTED);
	private ExecutorService executor;

	private java.net.ServerSocket socket;
	protected final Map<String, ClientSocket> clients = Collections.synchronizedMap(new HashMap<String, ClientSocket>());

	protected final ServerSocketOptions options = new ServerSocketOptions();

	protected final ServerSocketObserver observer = new ServerSocketObserver();

	/**
	 * @param listener
	 */
	public void add(final ServerSocketListener listener) {
		this.observer.add(listener);
	}

	/**
	 *
	 */
	public void clear() {
		this.observer.clear();
	}

	/**
	 * @param listener
	 */
	public void remove(final ServerSocketListener listener) {
		this.observer.remove(listener);
	}

	/**
	 * Sends a message to all the connected clients
	 *
	 * @param data
	 */
	public void send(final String data) {
		this.send(data, null);
	}

	/**
	 * Sends a message to the specific client if exists
	 *
	 * @param data
	 * @param address
	 */
	public void send(final String data, final String address) {
		if (!StringUtils.isBlank(data)) {
			if (address == null) {
				this.clients.values().parallelStream().forEach(client -> client.send(data));
			} else if (this.clients.containsKey(address)) {
				this.clients.get(address).send(data);
			}
		}
	}

	/**
	 *
	 */
	public void start() {
		if (ServerSocketStatus.DISCONNECTED.equals(this.status.get()) && this.executor == null) {
			this.executor = Executors.newSingleThreadExecutor();

			this.executor.execute(() -> {
				try {
					this.observer.notifyOnStart();
					this.process();
				} catch (final Exception e) {
					ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
					ServerSocket.LOG.error(e.getLocalizedMessage());
				}
			});
		}
	}

	/**
	 *
	 */
	public void stop() {
		this.options.setAutoReopen(false);

		if (!ServerSocketStatus.DISCONNECTED.equals(this.status.get())) {
			this.status.set(ServerSocketStatus.DISCONNECTED);
			this.observer.notifyOnStop();
		}

		this.clear();

		this.executor.shutdown();
		try {
			this.executor.awaitTermination(this.options.getTimeout(), TimeUnit.MILLISECONDS);
		} catch (final Exception e) {
			ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
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
			ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
		}
	}

	/**
	 *
	 */
	private void connect() {
		final AtomicReference<Exception> exception = new AtomicReference<>();

		final RetryBuilder builder = new RetryBuilder()//
				.withSleep(this.options.getTimeout())//
				.withMethod(() -> {
					try {
						this.status.set(ServerSocketStatus.STARTING);
						this.observer.notifyOnStarting();

						final int port = this.options.getPort();

						final java.net.ServerSocket socket = new java.net.ServerSocket(port);
						socket.setSoTimeout(this.options.getTimeout());

						this.socket = socket;

						this.status.set(ServerSocketStatus.LISTENING);
						this.observer.notifyOnListening();
					} catch (final Exception e) {
						ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
						exception.set(new Exception("Unable to start listening on port " + this.options.getPort() + ".", e));
						this.observer.notifyOnStartFailed();
						throw new RuntimeException(e);
					}
				});

		if (!this.options.isAutoReopen()) {
			builder.withAttempts(3);
		}

		try {
			builder.build().run();
		} catch (final Exception e) {
			ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
			ServerSocket.LOG.error(e.getLocalizedMessage());
			this.status.set(ServerSocketStatus.DISCONNECTED);
			this.observer.notifyOnDisconnected();
		}
	}

	/**
	 *
	 */
	private void listen() {
		while (ServerSocketStatus.LISTENING.equals(this.status.get())) {
			try {
				if (this.options.getClients() > this.clients.size()) {
					final Socket acceptedSocket = this.socket.accept();
					final String address = acceptedSocket.getInetAddress().getHostAddress();

					final ClientSocket clientSocket = new ClientSocketBuilder()//
							.withSocket(acceptedSocket)//
							.withAutoReconnect(false)//
							.withReadTries(this.options.getReadTries())//
							.withWriteTries(this.options.getWriteTries())//
							.withTimeout(this.options.getTimeout())//
							.build();

					final ServerSocketClientListener listener = new ServerSocketClientListener(this, clientSocket);
					clientSocket.add(listener);

					clientSocket.start();
					this.clients.put(address, clientSocket);
				} else {
					this.sleep();
				}
			} catch (final Exception e) {
				ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 *
	 */
	private void process() {
		this.connect();
		this.listen();

		this.close();

		if (this.options.isAutoReopen()) {
			this.sleep();
			this.process();
		} else {
			this.stop();
		}
	}

	/**
	 *
	 */
	private void sleep() {
		try {
			Thread.sleep(this.options.getTimeout());
		} catch (final Exception e) {
			ServerSocket.LOG.debug(e.getLocalizedMessage(), e);
		}
	}
}
