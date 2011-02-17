package com.garaio.fisheye.plugin.gerrit.gerritAccess;

import com.garaio.fisheye.plugin.gerrit.ssh.LineHandler;
import com.garaio.fisheye.plugin.gerrit.ssh.SshCommandExecutor;
import com.garaio.fisheye.plugin.gerrit.ssh.SshConfiguration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GerritAccessor {
    private SshConfiguration configuration;

    public GerritAccessor(SshConfiguration configuration) {
        this.configuration = configuration;
    }

    public Set<String> getProjects() throws IOException {
        final Set<String> result = new HashSet<String>();

        SshCommandExecutor commandExecutor = new SshCommandExecutor(configuration);
        commandExecutor.ExecuteCommand("gerrit ls-projects", new LineHandler() {
            public void HandleLine(String line) {
                result.add(line);
            }
        });


        return result;
    }
}

