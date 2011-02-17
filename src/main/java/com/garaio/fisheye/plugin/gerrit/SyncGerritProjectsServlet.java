package com.garaio.fisheye.plugin.gerrit;

import com.atlassian.fisheye.spi.admin.data.AuthenticationData;
import com.atlassian.fisheye.spi.admin.data.AuthenticationStyle;
import com.atlassian.fisheye.spi.admin.data.GitRepositoryData;
import com.atlassian.fisheye.spi.admin.data.RepositoryData;
import com.atlassian.fisheye.spi.admin.services.RepositoryAdminService;
import com.atlassian.fisheye.spi.admin.services.RepositoryConfigException;
import com.garaio.fisheye.plugin.gerrit.gerritAccess.GerritAccessor;
import com.garaio.fisheye.plugin.gerrit.ssh.SshConfiguration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class SyncGerritProjectsServlet extends HttpServlet {
    RepositoryAdminService repositoryAdminService;

    public void setRepositoryAdminService(RepositoryAdminService repositoryAdminService) {
        this.repositoryAdminService = repositoryAdminService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        PrintWriter writer = response.getWriter();

        Set<String> fishEyeRepoNames = repositoryAdminService.getNames();

        GerritAccessor gerritAccessor = new GerritAccessor(GetSshConfiguration());

        Set<String> gerritRepoNames = gerritAccessor.getProjects();
        for (String gerritRepoName : gerritRepoNames) {
            String fishEyeRepoName = GetFishEyeRepoName(gerritRepoName);
            if (!fishEyeRepoNames.contains(fishEyeRepoName)) {
                try {
                    repositoryAdminService.create(MakeRepo(GetSshConfiguration(), fishEyeRepoName, gerritRepoName));
                    repositoryAdminService.enable(fishEyeRepoName);
                    repositoryAdminService.start(fishEyeRepoName);
                    writer.printf("SUCCESS: Added Repository '%1$s' for gerrit:%2$s\n", fishEyeRepoName, gerritRepoName);
                } catch (Exception e) {
                    writer.printf("ERROR: Could not add Repository '%1$s' for gerrit:%2$s: %3$s\n", fishEyeRepoName, gerritRepoName, e.toString());
                }
            }
            else
            {
                fishEyeRepoNames.remove(fishEyeRepoName);
            }
        }

        for (String remainingFishEyeRepoName : fishEyeRepoNames) {
            if (!IsGerritRepo(remainingFishEyeRepoName)) {
                // do not touch repositories that are not manages by Gerrit
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

    private boolean IsGerritRepo(String remainingFishEyeRepoName) {
        RepositoryData repoData = repositoryAdminService.getRepositoryData(remainingFishEyeRepoName);
        if (repoData.getLocationDescription().startsWith(GetGitUrlForRepo(GetSshConfiguration(), ""))) {
            return true;
        }
        else
        {
            return false;
        }
    }

    private SshConfiguration GetSshConfiguration() {
        SshConfiguration sshConfig = new SshConfiguration();
        sshConfig.setHostName("sourcecontrol2");
        sshConfig.setUserName("lvw0149");
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEoQIBAAKCAQEAuGhL1L+XHQuG6SRgCUsUhj/SRqrE45JoRHTC4EBtcWcUr29J\n" +
                "B8qNbP0NDuYrX7d5qMyyG7lpQcJg68CO1XGLCvnA/w33CVinlQToSiG6gx6KJ+T0\n" +
                "/qVCOpZTseOJszJvP6idprDqxXySTbwoPRVZbC2U3BFZoC72wNAvhKx6o/FTzIwo\n" +
                "JkQpPwHb3E++Cb1PDLpYYVqWobsd7c3uHfV213hMLCzCJ15IAeYE+kNWTPXMbEOa\n" +
                "GgA0N8cX3V1f+bJs7lANE/9b0sm54LaA9GhcfTmL+vU3u18bqbe1Ymju171Qj1n+\n" +
                "UczCppnbemX3JvMb704OGM72X5WxJRhqt00hPQIBIwKCAQBZkb5upjN0gfESJ5xc\n" +
                "SQn4EF7R3eqaa678rb29YR83FM+IaUC6pDYQXadBwEDz+gfkRiqfwHT0DfSPw+2a\n" +
                "32DK0RSZJApGXkLEuT2gWYZ6M2es+i3axUwN0/zEHg+uz1qbQ0VCVe5f7AyMKDDN\n" +
                "NkFgbek3sKfgFs+few/EGULh6WuMCcc7i09+SG4H02XsAetmOuZxrhz14EUGT/Ta\n" +
                "Es1XEJgf6RWEhVCKk97NSspC6fWTt1tBFDFTfhV35hyt2cEkxre3roV702am+gzS\n" +
                "spDfaCk+dSGF+Cr2e5kgi3YQBo+LfbYhRM3T8R495N2r4wRUy947uoIFBt/q5H9B\n" +
                "BSUrAoGBAN0NainAL6NbewBycEDXmXF8LnEMKrf1LHtm4twwGUGhMHZD86w+oIaV\n" +
                "2I5vH8cMqxvhsmJ6x5/qoowEJMmTXBQWQWUw+7ba2RVkGvyvo4a/xdHjgH38MkNi\n" +
                "Y9kUcYHguNUGv0AUUXD3w2jPwAtoLDJaqo4asQZtIoAUH0dEyPN5AoGBANWPwpFj\n" +
                "9bUMtYBo0eeXiVNFmisBulfBZozjVYM78buhJavEpIAyQd9dzfcgtsGneBFLYwNG\n" +
                "aVvsil+mIpeW/3DXorUsnFXp8hxwIDyxGPbEUfvECxpgD8ser78pFIj0JCbS1Ckm\n" +
                "KeYnqcr+CvYvt0oTPAG/YbcVRuuWyFmwaoblAoGAa15Jgf5DBjO/Zp38AjzVgEOh\n" +
                "h12uWVnTxujUltWInDhZXgO/f4wiFXv7eGHcPBwYmIORKITHXE1k5OwR3kBCqqu5\n" +
                "XQkpzdgDCmPSl/2J8P4PoHXQsjiqswPnWs9qVQbALPSmCS5wszaDfA0iw7ZBWklL\n" +
                "htnDsqoJcWjb/gt3jDMCgYEAz3W1sc7RbgxYi16Cw7fHOvMoDIVV7tkhyrDz+9PU\n" +
                "4ieDriy9DtG8VVPPZRh3BUOZNV8s+93/8uXIP6iz47dBSQv9JQbDwSxniVb6vp1o\n" +
                "tTO9Wvj018sIByUfwQNVxt6JhMzOGVhF9YWdoJ8DVY1wOVSDb2l03bzlzuo++Ap2\n" +
                "HKMCgYAu4qKbNU3RR2438Qy4rPIiOO1IUWmgwM8VNfq5Ic8FCn7xVsfnjYBWT1E6\n" +
                "Qx9eixOFXPYZK+LvS/0/fQRr3tC6X4NfaClG8sDvdgqGCFlf8zW4PDYoSHqbXxh9\n" +
                "Gma18UZmo8WrYCGsmKyCtSAkrN0YhVpUvMSO6qkkWN3jlrLvxQ==\n" +
                "-----END RSA PRIVATE KEY-----\n\n";
        sshConfig.setPrivateKey(privateKey);
        sshConfig.setPort(29418);
        return sshConfig;
    }

    private String GetFishEyeRepoName(String repoName) {
        return repoName.replaceAll("[^a-zA-Z0-9_\\.]", "-");
    }

    private RepositoryData MakeRepo(SshConfiguration sshConfig, String fisheEyeRepoName, String gerritRepoName) {
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