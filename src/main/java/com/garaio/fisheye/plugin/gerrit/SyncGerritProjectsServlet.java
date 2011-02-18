package com.garaio.fisheye.plugin.gerrit;

import com.atlassian.fisheye.spi.admin.data.AuthenticationData;
import com.atlassian.fisheye.spi.admin.data.AuthenticationStyle;
import com.atlassian.fisheye.spi.admin.data.GitRepositoryData;
import com.atlassian.fisheye.spi.admin.data.RepositoryData;
import com.atlassian.fisheye.spi.admin.services.RepositoryAdminService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.garaio.fisheye.plugin.gerrit.gerritAccess.GerritAccessor;
import com.garaio.fisheye.plugin.gerrit.ssh.SshConfiguration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Set;

public class SyncGerritProjectsServlet extends HttpServlet {
    private final RepositoryAdminService repositoryAdminService;
    private final PluginSettingsFactory settingsFactory;

    public SyncGerritProjectsServlet(PluginSettingsFactory settingsFactory, RepositoryAdminService repositoryAdminService) {
        this.repositoryAdminService = repositoryAdminService;
        this.settingsFactory = settingsFactory;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();

        Set<String> fishEyeRepoNames = repositoryAdminService.getNames();

        SshConfiguration sshConfiguration = SshConfiguration.getConfig(settingsFactory.createGlobalSettings());
        GerritAccessor gerritAccessor = new GerritAccessor(sshConfiguration);

        Set<String> gerritRepoNames = gerritAccessor.getProjects();
        for (String gerritRepoName : gerritRepoNames) {
            String fishEyeRepoName = GetFishEyeRepoName(gerritRepoName);
            if (!fishEyeRepoNames.contains(fishEyeRepoName)) {
                try {
                    repositoryAdminService.create(MakeRepo(sshConfiguration, fishEyeRepoName, gerritRepoName));
                    repositoryAdminService.enable(fishEyeRepoName);
                    repositoryAdminService.start(fishEyeRepoName);
                    writer.printf("SUCCESS: Added Repository '%1$s' for gerrit:%2$s\n", fishEyeRepoName, gerritRepoName);
                } catch (Exception e) {
                    writer.printf("ERROR: Could not add Repository '%1$s' for gerrit:%2$s: %3$s\n", fishEyeRepoName, gerritRepoName, e.toString());
                }
            } else {
                fishEyeRepoNames.remove(fishEyeRepoName);
            }
        }

        for (String remainingFishEyeRepoName : fishEyeRepoNames) {
            if (!IsGerritRepo(sshConfiguration, remainingFishEyeRepoName)) {
                // do not touch repositories that are not managed by Gerrit
                continue;
            }

            try {
                repositoryAdminService.stop(remainingFishEyeRepoName);
            } catch (Exception e) {
                // probably invalid state. try to disable and delete anyway.
            }
            try {
                repositoryAdminService.disable(remainingFishEyeRepoName);
            } catch (Exception e) {
                // probably invalid state. try to delete anyway.
            }
            try {
                repositoryAdminService.delete(remainingFishEyeRepoName);
                writer.printf("SUCCESS: Removed Repository '%1$s'\n", remainingFishEyeRepoName);
            } catch (Exception e) {
                writer.printf("ERROR: Could not delete Repository '%1$s': %2$s\n", remainingFishEyeRepoName, e.toString());
            }
        }
    }

    private boolean IsGerritRepo(SshConfiguration sshConfiguration, String remainingFishEyeRepoName) {
        RepositoryData repoData = repositoryAdminService.getRepositoryData(remainingFishEyeRepoName);
        if (repoData.getLocationDescription().startsWith(GetGitUrlForRepo(sshConfiguration, ""))) {
            return true;
        } else {
            return false;
        }
    }

    private String GetFishEyeRepoName(String repoName) {
        return repoName.replaceAll("[^a-zA-Z0-9_\\.]", "-");
    }

    private RepositoryData MakeRepo(SshConfiguration sshConfig, String fisheEyeRepoName, String gerritRepoName) throws IOException {
        GitRepositoryData result = new GitRepositoryData(fisheEyeRepoName, GetGitUrlForRepo(sshConfig, gerritRepoName));
        result.setStoreDiff(true);
        result.setRenameOption(3);
        AuthenticationData authentication = new AuthenticationData();
        authentication.setPrivateKey(sshConfig.getPrivateKey());
        authentication.setAuthenticationStyle(AuthenticationStyle.SSH_KEY_WITHOUT_PASSPHRASE);
        result.setAuthentication(authentication);
        return result;
    }

    private static String GetGitUrlForRepo(SshConfiguration sshConfig, String gerritRepoName) {
        return String.format((Locale) null, "ssh://%1$s@%2$s:%3$s/%4$s", sshConfig.getUserName(), sshConfig.getHostName(), Integer.toString(sshConfig.getPort()), gerritRepoName);
    }
}