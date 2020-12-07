package com.zhangwanli.test.gradle.plugin;

import java.util.List;

public class EnvFilePluginExtension {

    public List<String> getEnvFiles() {
        return envFiles;
    }

    public void setEnvFiles(List<String> envFiles) {
        this.envFiles = envFiles;
    }

    private List<String> envFiles;

}
