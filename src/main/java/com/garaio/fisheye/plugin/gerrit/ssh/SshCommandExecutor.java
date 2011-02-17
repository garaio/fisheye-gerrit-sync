package com.garaio.fisheye.plugin.gerrit.ssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.garaio.fisheye.plugin.gerrit.ssh.SshConfiguration;
import com.garaio.fisheye.plugin.gerrit.ssh.LineHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: lvw0149
 * Date: 17.02.2011
 * Time: 09:31:59
 * To change this template use File | Settings | File Templates.
 */
public class SshCommandExecutor {
    private final SshConfiguration configuration;

    public SshCommandExecutor(SshConfiguration configuration) {
        this.configuration = configuration;
    }

    public void ExecuteCommand(String command, LineHandler lineHandler) throws IOException {
        Connection conn = new Connection(configuration.getHostName(), configuration.getPort());
        conn.connect();
        try {

            boolean authOk = conn.authenticateWithPublicKey(configuration.getUserName(), configuration.getPrivateKey().toCharArray(), configuration.getPassPhrase());
            if (!authOk) {
                throw new IOException("Authentication failed!");
            }

            Session sess = conn.openSession();
            try {
                sess.execCommand("gerrit ls-projects");

                InputStream stdout = new StreamGobbler(sess.getStdout());

                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

                System.out.println("Here is some information about the remote host:");

                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    lineHandler.HandleLine(line);
                }

            }
            finally {
                /* Close this session */

                sess.close();
            }

            /* Close the connection */

        }
        finally {
            conn.close();
        }

    }
}
