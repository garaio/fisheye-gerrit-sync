package com.garaio.fisheye.plugin.gerrit.ssh;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class SshConfiguration {
    String hostName;
    int port;
    String userName;
    String privateKey;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public static SshConfiguration getConfig(PluginSettings settings) {
        SshConfiguration config = new SshConfiguration();
        config.hostName = GetSettingOrDefault(settings, "com.garaio.fisheye.plugin.gerrit.ssh.hostname", "");
        config.port = Integer.parseInt(GetSettingOrDefault(settings, "com.garaio.fisheye.plugin.gerrit.ssh.port", "29418"));
        config.userName = GetSettingOrDefault(settings, "com.garaio.fisheye.plugin.gerrit.ssh.username", "");
        config.privateKey = GetSettingOrDefault(settings, "com.garaio.fisheye.plugin.gerrit.ssh..privatekeyfile", "");
        return config;
    }

    private static String GetSettingOrDefault(PluginSettings settings, String settingName, String defaultValue) {
        String setting = (String) settings.get(settingName);
        return setting != null ? setting : defaultValue;
    }

    public void storeTo(PluginSettings settings) {
        settings.put("com.garaio.fisheye.plugin.gerrit.ssh.hostname", hostName);
        settings.put("com.garaio.fisheye.plugin.gerrit.ssh.port", Integer.toString(port));
        settings.put("com.garaio.fisheye.plugin.gerrit.ssh.username", userName);
        settings.put("com.garaio.fisheye.plugin.gerrit.ssh..privatekeyfile", privateKey);
    }
}
