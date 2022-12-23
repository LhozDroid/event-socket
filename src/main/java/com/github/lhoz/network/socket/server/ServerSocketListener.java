/**
 *
 */
package com.github.lhoz.network.socket.server;

/**
 * @author Lhoz
 *
 */
public interface ServerSocketListener {
	/**
	 *
	 * @param address
	 */
	void onClientConnected(String address);

	/**
	 *
	 * @param address
	 */
	void onClientDisconnected(String address);

	/**
	 *
	 * @param address
	 * @param data
	 */
	void onDataReceived(String address, String data);

	/**
	 * @param address
	 * @param data
	 */
	void onDataSent(String address, String data);

	/**
	 *
	 */
	void onDisconnected();

	/**
	 *
	 */
	void onListening();

	/**
	 *
	 */
	void onStart();

	/**
	 *
	 */
	void onStartFailed();

	/**
	 *
	 */
	void onStarting();

	/**
	 *
	 */
	void onStop();
}
