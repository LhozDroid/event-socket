/**
 *
 */
package dev.lhoz.socket.es.client;

/**
 * @author Lhoz
 *
 */
public abstract class ClientSocketListener {
	/**
	 *
	 */
	public abstract void onConnected();

	/**
	 *
	 */
	public abstract void onConnecting();

	/**
	 *
	 */
	public abstract void onConnectionFailed();

	/**
	 * @param data
	 */
	public abstract void onDataReceived(String data);

	/**
	 * @param data
	 */
	public abstract void onDataSent(String data);

	/**
	 *
	 */
	public abstract void onDisconnected();

	/**
	 *
	 */
	public abstract void onStart();

	/**
	 *
	 */
	public abstract void onStop();
}
