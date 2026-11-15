package com.maifee.chirkut.network;

import com.maifee.chirkut.util.Default;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Outbound TCP channel to one peer. Accepts {@code "host"} or
 * {@code "host:port"} as the peer address so emulator setups can tunnel
 * the default port. On construction, writes a single {@code HELLO|<me>}
 * handshake line so the receiver can route replies back to us.
 * @author Maifee Ul Asad
 */
public class Client {

    private static final int CONNECT_TIMEOUT_MS = 5_000;

    public final Socket socket;
    public final String hostAddress;
    public final String remoteHost;
    public final int remotePort;

    public Client(String hostAddress) throws IOException {
        this(hostAddress, hostAddress);
    }

    /**
     * @param hostAddress the peer identifier (e.g. {@code "127.0.0.1:9000"}).
     * @param selfAddress the address the peer should use to dial us back;
     *                    sent as the HELLO handshake.
     */
    public Client(String hostAddress, String selfAddress) throws IOException {
        this.hostAddress = hostAddress;
        int sep = hostAddress.indexOf(':');
        if (sep >= 0) {
            this.remoteHost = hostAddress.substring(0, sep);
            this.remotePort = Integer.parseInt(hostAddress.substring(sep + 1));
        } else {
            this.remoteHost = hostAddress;
            this.remotePort = Default.PORT;
        }
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(remoteHost, remotePort), CONNECT_TIMEOUT_MS);
        writeLine("HELLO|" + selfAddress);
    }

    /** Sends [message] as one line (LF-terminated). */
    public void send(String message) throws IOException {
        writeLine(message);
    }

    private synchronized void writeLine(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}
