/**
 *
 */
package dev.lhoz.network.es.example;

import dev.lhoz.network.es.server.ServerSocket;
import dev.lhoz.network.es.server.ServerSocketBuilder;

/**
 * @author Lhoz
 *
 */
public class ServerExample {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final ServerSocket server = new ServerSocketBuilder()//
				.withAutoReopen(true)//
				.withClients(3)//
				.withTimeout(250)//
				.withPort(9797)//
				.build();

		final ServerListener listener = new ServerListener();
		server.add(listener);

		server.start();

		while (true) {
			server.send("Hello client, from server!");

			try {
				Thread.sleep(10000);
			} catch (final Exception e) {
			}
		}
	}

}
