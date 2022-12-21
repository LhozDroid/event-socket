/**
 *
 */
package dev.lhoz.socket.es.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lhoz
 *
 */
public class ClientSocketObserver {
	private final Set<ClientSocketListener> listeners = Collections.synchronizedSet(new HashSet<ClientSocketListener>());

	/**
	 * @param listener
	 */
	public void add(final ClientSocketListener listener) {
		if (listener != null) {
			this.listeners.add(listener);
		}
	}

	/**
	 *
	 */
	public void clear() {
		this.listeners.clear();
	}

	/**
	 *
	 */
	public void notifyOnConnected() {
		this.listeners.parallelStream().forEach(ClientSocketListener::onConnected);
	}

	/**
	 *
	 */
	public void notifyOnConnecting() {
		this.listeners.parallelStream().forEach(ClientSocketListener::onConnecting);
	}

	/**
	 *
	 */
	public void notifyOnConnectionFailed() {
		this.listeners.parallelStream().forEach(ClientSocketListener::onConnectionFailed);
	}

	/**
	 * @param data
	 */
	public void notifyOnDataReceived(final String data) {
		this.listeners.parallelStream().forEach(listener -> listener.onDataReceived(data));
	}

	/**
	 * @param data
	 */
	public void notifyOnDataSent(final String data) {
		this.listeners.parallelStream().forEach(listener -> listener.onDataSent(data));
	}

	/**
	 *
	 */
	/**
	 *
	 */
	public void notifyOnDisconnected() {
		this.listeners.parallelStream().forEach(ClientSocketListener::onDisconnected);
	}

	/**
	 *
	 */
	public void notifyOnStart() {
		this.listeners.parallelStream().forEach(ClientSocketListener::onStart);
	}

	/**
	 *
	 */
	public void notifyOnStop() {
		this.listeners.parallelStream().forEach(ClientSocketListener::onStop);
	}

	/**
	 * @param listener
	 */
	public void remove(final ClientSocketListener listener) {
		if (this.listeners.contains(listener)) {
			this.listeners.remove(listener);
		}
	}
}
