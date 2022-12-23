/**
 *
 */
package dev.lhoz.network.es.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ClientSocketReader {
	private BufferedReader bufferedReader;
	private InputStream inputStream;
	private InputStreamReader inputStreamReader;

	/**
	 * @param socket
	 * @throws Exception
	 */
	public ClientSocketReader(final Socket socket) throws Exception {
		try {
			this.inputStream = socket.getInputStream();
			this.inputStreamReader = new InputStreamReader(this.inputStream);
			this.bufferedReader = new BufferedReader(this.inputStreamReader);
		} catch (final Exception e) {
			ClientSocketReader.LOG.debug(e.getLocalizedMessage(), e);
			ClientSocketReader.LOG.error(e.getLocalizedMessage());
			throw new Exception("Unable to create the socket reader. " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 *
	 */
	public void close() {
		try {
			this.bufferedReader.close();
		} catch (final Exception e) {
			ClientSocketReader.LOG.debug(e.getLocalizedMessage(), e);
		}

		try {
			this.inputStreamReader.close();
		} catch (final Exception e) {
			ClientSocketReader.LOG.debug(e.getLocalizedMessage(), e);
		}

		try {
			this.bufferedReader.close();
		} catch (final Exception e) {
			ClientSocketReader.LOG.debug(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String read() throws Exception {
		String data = StringUtils.EMPTY;

		try {
			final String line = this.bufferedReader.readLine();
			if (line == null) {
				throw new Exception("Socket disconnected.");
			}
			if (!StringUtils.isBlank(line)) {
				data = StringUtils.trim(line);
			}
		} catch (final SocketTimeoutException e) {
			ClientSocketReader.LOG.debug(e.getLocalizedMessage(), e);
		} catch (final Exception e) {
			ClientSocketReader.LOG.debug(e.getLocalizedMessage(), e);
			ClientSocketReader.LOG.error(e.getLocalizedMessage());
			throw new Exception("Error reading data from socket. " + e.getLocalizedMessage(), e);
		}

		return data;
	}

}
