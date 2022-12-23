/**
 *
 */
package com.github.lhoz.network.socket.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lhoz
 *
 */
public class ServerSocketObserver {
	private final Set<ServerSocketListener> listeners = Collections.synchronizedSet(new HashSet<ServerSocketListener>());

	/**
	 * @param listener
	 */
	public void add(final ServerSocketListener listener) {
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
	 * @param address
	 */
	public void notifyOnClientConnected(final String address) {
		this.listeners.parallelStream().forEach(listener -> listener.onClientConnected(address));
	}

	/**
	 *
	 * @param address
	 */
	public void notifyOnClientDisconnected(final String address) {
		this.listeners.parallelStream().forEach(listener -> listener.onClientDisconnected(address));
	}

	/**
	 * @param address
	 * @param data
	 */
	public void notifyOnDataReceived(final String address, final String data) {
		this.listeners.parallelStream().forEach(listener -> listener.onDataReceived(address, data));
	}

	/**
	 * @param address
	 * @param data
	 */
	public void notifyOnDataSent(final String address, final String data) {
		this.listeners.parallelStream().forEach(listener -> listener.onDataSent(address, data));
	}

	/**
	 *
	 */
	public void notifyOnDisconnected() {
		this.listeners.parallelStream().forEach(ServerSocketListener::onDisconnected);
	}

	/**
	 *
	 */
	public void notifyOnListening() {
		this.listeners.parallelStream().forEach(ServerSocketListener::onListening);
	}

	/**
	 *
	 */
	public void notifyOnStart() {
		this.listeners.parallelStream().forEach(ServerSocketListener::onStart);
	}

	/**
	 *
	 */
	public void notifyOnStartFailed() {
		this.listeners.parallelStream().forEach(ServerSocketListener::onStartFailed);
	}

	/**
	 *
	 */
	public void notifyOnStarting() {
		this.listeners.parallelStream().forEach(ServerSocketListener::onStarting);
	}

	/**
	 *
	 */
	public void notifyOnStop() {
		this.listeners.parallelStream().forEach(ServerSocketListener::onStop);
	}

	/**
	 * @param listener
	 */
	public void remove(final ServerSocketListener listener) {
		if (this.listeners.contains(listener)) {
			this.listeners.remove(listener);
		}
	}
}
