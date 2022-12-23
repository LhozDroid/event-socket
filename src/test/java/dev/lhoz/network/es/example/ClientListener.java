/**
 *
 */
package dev.lhoz.network.es.example;

import dev.lhoz.network.es.client.ClientSocketListener;
import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ClientListener implements ClientSocketListener {

	@Override
	public void onConnected() {
		ClientListener.LOG.info("Connected");
	}

	@Override
	public void onConnecting() {
		ClientListener.LOG.info("Connecting");
	}

	@Override
	public void onConnectionFailed() {
		ClientListener.LOG.info("Connection failed");
	}

	@Override
	public void onDataReceived(final String data) {
		ClientListener.LOG.info("Received: " + data);
	}

	@Override
	public void onDataSent(final String data) {
		ClientListener.LOG.info("Sent: " + data);
	}

	@Override
	public void onDisconnected() {
		ClientListener.LOG.info("Disconnected");
	}

	@Override
	public void onStart() {
		ClientListener.LOG.info("Start");
	}

	@Override
	public void onStop() {
		ClientListener.LOG.info("Stop");
	}

}
