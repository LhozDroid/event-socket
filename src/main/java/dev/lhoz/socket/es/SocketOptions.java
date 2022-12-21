/**
 *
 */
package dev.lhoz.socket.es;

import lombok.Data;

/**
 * @author Lhoz
 *
 */
@Data
public class SocketOptions {
	private String address = "127.0.0.1";
	private int port = 65535;

	private int timeout = 250;

	private boolean autoReconnect = false;

	private int readTries = -1;
	private int writeTries = -1;
}
