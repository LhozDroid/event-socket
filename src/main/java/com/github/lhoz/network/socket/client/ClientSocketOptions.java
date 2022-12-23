/**
 *
 */
package com.github.lhoz.network.socket.client;

import lombok.Data;

/**
 * @author Lhoz
 *
 */
@Data
public class ClientSocketOptions {
	private String address = "127.0.0.1";
	private int port = 65535;

	private int timeout = 10;

	private boolean autoReconnect = false;

	private int readTries = 1;
	private int writeTries = 1;
}
