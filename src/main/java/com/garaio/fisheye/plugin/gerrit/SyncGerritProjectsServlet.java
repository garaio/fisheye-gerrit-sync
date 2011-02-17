package com.garaio.fisheye.plugin.gerrit;

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
        writer.print("<h1>Currently in FishEye</h1>");
        writer.print("<ul>");

        Set<String> repoNames = repositoryAdminService.getNames();
        for (String repoName : repoNames) {
            writer.print("<li>" + repoName + "</li>");
        }

        writer.print("</ul>");

        SshConfiguration sshConfig = new SshConfiguration();
        sshConfig.setHostName("sourcecontrol2");
        sshConfig.setUserName("lvw0149");
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "Proc-Type: 4,ENCRYPTED\n" +
                "DEK-Info: DES-EDE3-CBC,948D2CB1DACA2DA0\n" +
                "\n" +
                "7J30MbS4OOp/hDwAYmeoS4ZtUheaiOy0UQwkX0VHcuiC3qVFwOCM8qeVuTY9MDPZ\n" +
                "uelbOuDl/eWu6plR91bzWnUOxWH9AwzDY6fOljQxonV96OTXpBM3zhurUpy3Ab83\n" +
                "ScLWnQWMnXOz37v93ARylMO0wWumE8UihTNTgh5WgWs4N4o3ZA16wuTgr0nnC5yq\n" +
                "/7B6PP9L2rgVgzUzsaZmfkCQJHBqqMsp2zcbbEyrvjUEbQJ0DPJJxkKOZWm/lPsN\n" +
                "t8A010kmVSo1mzSG00LXm0kdOqE2u3b9Fc4TQ/xjxixmFkxtckrPU/KrSticS/pC\n" +
                "Yg5ys1iBZMXTBrjjoQe7qnu44eB71EaRAVmjnbtcOaOC3siVJQO5mXgQsbCvwbk/\n" +
                "YTT0y03J6AU79K+QYoZLtr541ffkkxp3QiI0pu1o3Qn7q/4dOkyLUueEqAB3JCCD\n" +
                "fxdmZT1ShHhgiAMUoZQIUlhYGQf9m4yZEFL+6ZG9fE74cML2P2VFuUT2smbbWtUE\n" +
                "D3egZ+8f49HKKVEgRFVFHDAMEZNxy3aDwhMIX4DBJynJXYjnHCvIPvmQef69P2XR\n" +
                "3Dl0isVyi03Ggyv2z701KfvhuokJBgydmbuWGCojlIzQz9OBdD3wiLq2SshU7mhS\n" +
                "QlJK3RnjkMdmEiGe0Gzm70+uRudrosEXP0/mE4jQ1TJORdo5jo1QE6LhHmkaozJU\n" +
                "aPle8MbRAU5ugzgymMU1WAP8NGpd1qewJk0RjtQktfx2kk/jA1EcFQr+uqXqdObt\n" +
                "PKsufTQQSpY6EkB9R+S+TXv8AK/vXo8SgjMC1Ieav3VOv2+My/HwIrMVnO37cePH\n" +
                "jS1/gAVjR4tvjzorfhjzPv6Hohc5SQti9lTz26VHke9jD3wZpj7L9GQaAeDIBylj\n" +
                "PTydP3WyajJPMtdvNypca+nl5G4mCo1zz6Sff0Gdab9//KAF0wCToGHdKJBC/IgF\n" +
                "2jUORAdAItz2cL1mkV/oC+n73qEGJlztf/NKTNLrAvbOxt9rhZZGclKGiAykwvGe\n" +
                "xoo/UGmhTkVydGog6XjgHGJfi6HRSnw8Yc2xDKzpbzVZTyA6NiEfvXSTogpcnLKe\n" +
                "b+eUa9KD4ednddkEs8iRx8WsE3QiqyfPf+7+7YC9vRnPvuvb6wQo4Bu5XUfwJ8HK\n" +
                "8T8AfYhs5Tw8qJRwzlV7uEI/Hy++nkHM33KFJWHTHZuK+hdyat6WTQpjhvfwsYjB\n" +
                "kpRNjID0HFWYOenpfO6a7WqaLRXcWe10944cWboMgiRVgo0ofodHASXGLTelqNlR\n" +
                "pSl/uQ+9udvBhi4h43AP7HNW6NAQ4hKK4WglXiYlP4j3VXVspoqvajmDbzMphyat\n" +
                "vKdPCV2JTYaZtEtoWhgjEUieouD2gjaifa1FxoDCxXtibK09xgUAOpKs3VLGQwxW\n" +
                "ty2G7hWgu4FiMue5w71T6g8jJOI6r93iLKR41QgGOM00mNYU1okLz2uGTzDRQ4hh\n" +
                "gk6TKQnkxeUH/qT4Xqg8Wr86otNHJ+DMeZ7ziVnGMNbYYSoCHY39zh80AwDGpRlR\n" +
                "fkpfmzRO3XTms+K5zUk+3qLbNP/R1q05agj+PDVFbesomriQaqwEhA==\n" +
                "-----END RSA PRIVATE KEY-----\n\n";
        sshConfig.setPrivateKey(privateKey);
        sshConfig.setPassPhrase("FishEye");
        sshConfig.setPort(29418);

        GerritAccessor gerritAccessor = new GerritAccessor(sshConfig);
        writer.print("<h1>Currently in Gerrit</h1>");
        writer.print("<ul>");

        Set<String> gerritRepoNames = gerritAccessor.getProjects();
        for (String repoName : gerritRepoNames) {
            String fisheEyeRepoName = GetFishEyeRepoName(repoName);
            if (!repoNames.contains(fisheEyeRepoName)) {
                try {
                    repositoryAdminService.create(MakeRepo(fisheEyeRepoName, repoName));
                    repositoryAdminService.enable(fisheEyeRepoName);
                } catch (RepositoryConfigException e) {
                    throw new IOException(e.toString(), e);
                }
            }
            writer.print("<li>" + repoName + "</li>");
        }

        writer.print("</ul>");
    }

    private String GetFishEyeRepoName(String repoName) {
        return repoName.replaceAll("[^a-zA-Z0-9_\\.]", "-");
    }

    private RepositoryData MakeRepo(String fisheEyeRepoName, String gerritRepoName) {
        RepositoryData result = new GitRepositoryData(fisheEyeRepoName, "gerrit:" + gerritRepoName);
        result.setStoreDiff(true);
        return result;
    }
}