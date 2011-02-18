package com.garaio.fisheye.plugin.gerrit;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.garaio.fisheye.plugin.gerrit.gerritAccess.GerritAccessor;
import com.garaio.fisheye.plugin.gerrit.ssh.SshConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class ConfigGerritProjectsServlet extends HttpServlet {
    private final PluginSettingsFactory settingsFactory;

    private final TemplateRenderer templateRenderer;

    @Autowired
    public ConfigGerritProjectsServlet(PluginSettingsFactory settingsFactory, TemplateRenderer templateRenderer) {
        this.settingsFactory = settingsFactory;
        this.templateRenderer = templateRenderer;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SshConfiguration sshConfiguration = SshConfiguration.getConfig(settingsFactory.createGlobalSettings());
        RenderPage(request, response, sshConfiguration, "view", null);
    }

    private void RenderPage(HttpServletRequest request, HttpServletResponse response, SshConfiguration sshConfiguration, String mode, String message) throws IOException {
        request.setAttribute("decorator", "atl.admin");
        response.setContentType("text/html");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("sshConfig", sshConfiguration);
        params.put("message", message);
        templateRenderer.render("config-" + mode + ".vm", params, response.getWriter());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PluginSettings pluginSettings = settingsFactory.createGlobalSettings();
        SshConfiguration sshConfiguration = SshConfiguration.getConfig(pluginSettings);

        if (request.getParameter("port") == null || request.getParameter("port") == "") {
            // open edit form
            RenderPage(request, response, sshConfiguration, "edit", "");
            return;
        }

        sshConfiguration.setHostName(request.getParameter("hostName"));
        sshConfiguration.setPort(Integer.parseInt(request.getParameter("port")));
        sshConfiguration.setUserName(request.getParameter("userName"));
        sshConfiguration.setPrivateKey(request.getParameter("privateKey"));

        try {
            GerritAccessor accessor = new GerritAccessor(sshConfiguration);
            Set<String> projects = accessor.getProjects();
        }
        catch (IOException e) {
            // Config not valid!
            RenderPage(request, response, sshConfiguration, "edit", "Configuration invalid. Please check your settings.");
        }
        sshConfiguration.storeTo(pluginSettings);
        response.sendRedirect("config");
    }
}