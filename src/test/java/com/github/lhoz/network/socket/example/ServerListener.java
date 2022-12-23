/**
 *
 */
package com.github.lhoz.network.socket.example;

import com.github.lhoz.network.socket.server.ServerSocketListener;

import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ServerListener implements ServerSocketListener {

	@Override
	public void onClientConnected(final String address) {
		ServerListener.LOG.info("Connected: " + address);
	}

	@Override
	public void onClientDisconnected(final String address) {
		ServerListener.LOG.info("Disconnected: " + address);
	}

	@Override
	public void onDataReceived(final String address, final String data) {
		ServerListener.LOG.info("Received: " + address + " - " + data);
	}

	@Override
	public void onDataSent(final String address, final String data) {
		ServerListener.LOG.info("Sent: " + address + " - " + data);
	}

	@Override
	public void onDisconnected() {
		ServerListener.LOG.info("Disconnected");
	}

	@Override
	public void onListening() {
		ServerListener.LOG.info("Listening");
	}

	@Override
	public void onStart() {
		ServerListener.LOG.info("Start");
	}

	@Override
	public void onStartFailed() {
		ServerListener.LOG.info("Start failed");
	}

	@Override
	public void onStarting() {
		ServerListener.LOG.info("Starting");
	}

	@Override
	public void onStop() {
		ServerListener.LOG.info("Stop");
	}

}
