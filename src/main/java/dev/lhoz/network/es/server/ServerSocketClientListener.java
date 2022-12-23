/**
 *
 */
package dev.lhoz.network.es.server;

import dev.lhoz.network.es.client.ClientSocket;
import dev.lhoz.network.es.client.ClientSocketListener;

/**
 * @author Lhoz
 *
 */
public class ServerSocketClientListener implements ClientSocketListener {
	private final ServerSocket server;
	private final ClientSocket client;

	/**
	 * @param server
	 * @param client
	 */
	public ServerSocketClientListener(final ServerSocket server, final ClientSocket client) {
		this.server = server;
		this.client = client;
	}

	/**
	 *
	 */
	@Override
	public void onConnected() {
		this.server.observer.notifyOnClientConnected(this.client.getAddress());
	}

	/**
	 *
	 */
	@Override
	public void onConnecting() {
	}

	/**
	 *
	 */
	@Override
	public void onConnectionFailed() {
	}

	/**
	 *
	 *
	 * @param data
	 */
	@Override
	public void onDataReceived(final String data) {
		this.server.observer.notifyOnDataReceived(this.client.getAddress(), data);
	}

	/**
	 *
	 *
	 * @param data
	 */
	@Override
	public void onDataSent(final String data) {
		this.server.observer.notifyOnDataSent(this.client.getAddress(), data);
	}

	/**
	 *
	 */
	@Override
	public void onDisconnected() {
		final String address = this.client.getAddress();
		this.server.clients.remove(address);
		this.server.observer.notifyOnClientDisconnected(address);
	}

	/**
	 *
	 */
	@Override
	public void onStart() {
	}

	/**
	 *
	 */
	@Override
	public void onStop() {
	}

}