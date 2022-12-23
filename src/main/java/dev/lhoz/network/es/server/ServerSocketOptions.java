/**
 *
 */
package dev.lhoz.network.es.server;

import lombok.Data;

/**
 * @author Lhoz
 *
 */
@Data
public class ServerSocketOptions {
	private int port = 65535;

	private int timeout = 10;

	private boolean autoReopen = false;

	private int readTries = 1;
	private int writeTries = 1;

	private int clients = Integer.MAX_VALUE;
}
