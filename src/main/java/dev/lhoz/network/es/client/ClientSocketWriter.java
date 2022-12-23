/**
 *
 */
package dev.lhoz.network.es.client;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author Lhoz
 *
 */
@Log4j2
public class ClientSocketWriter {
	private BufferedWriter bufferedWriter;
	private OutputStream outputStream;
	private OutputStreamWriter outputStreamWriter;

	/**
	 * @param socket
	 * @throws Exception
	 */
	public ClientSocketWriter(final Socket socket) throws Exception {
		try {
			this.outputStream = socket.getOutputStream();
			this.outputStreamWriter = new OutputStreamWriter(this.outputStream);
			this.bufferedWriter = new BufferedWriter(this.outputStreamWriter);
		} catch (final Exception e) {
			ClientSocketWriter.LOG.debug(e.getLocalizedMessage(), e);
			ClientSocketWriter.LOG.error(e.getLocalizedMessage());
			throw new Exception("Unable to create the socket writer. " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 *
	 */
	public void close() {
		try {
			this.bufferedWriter.close();
		} catch (final Exception e) {
			ClientSocketWriter.LOG.debug(e.getLocalizedMessage(), e);
		}

		try {
			this.outputStreamWriter.close();
		} catch (final Exception e) {
			ClientSocketWriter.LOG.debug(e.getLocalizedMessage(), e);
		}

		try {
			this.outputStream.close();
		} catch (final Exception e) {
			ClientSocketWriter.LOG.debug(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * @param data
	 * @throws Exception
	 */
	public void write(final String data) throws Exception {
		try {
			this.bufferedWriter.write(data + StringUtils.LF);
			this.bufferedWriter.flush();
		} catch (final Exception e) {
			ClientSocketWriter.LOG.debug(e.getLocalizedMessage(), e);
			ClientSocketWriter.LOG.error(e.getLocalizedMessage());
			throw new Exception("Error writing data to socket. " + e.getLocalizedMessage(), e);
		}
	}
}