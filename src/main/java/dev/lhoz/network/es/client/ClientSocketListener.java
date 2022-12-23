/**
 *
 */
package dev.lhoz.network.es.client;

/**
 * @author Lhoz
 *
 */
public interface ClientSocketListener {
	/**
	 *
	 */
	void onConnected();

	/**
	 *
	 */
	void onConnecting();

	/**
	 *
	 */
	void onConnectionFailed();

	/**
	 * @param data
	 */
	void onDataReceived(String data);

	/**
	 * @param data
	 */
	void onDataSent(String data);

	/**
	 *
	 */
	void onDisconnected();

	/**
	 *
	 */
	void onStart();

	/**
	 *
	 */
	void onStop();
}
