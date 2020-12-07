package com.sap.s4hana.eureka.test.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class EnvFilePlugin implements Plugin<Project> {

    private static final String LOOPBACK_IP = "127.0.0.1";

    @Override
    public void apply(Project project) {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();

        EnvFilePluginExtension extension = project.getExtensions().create("envFile",EnvFilePluginExtension.class);

        // Add a task that uses configuration from the extension object
        project.getTasks().register("generateLocalEnvFile", task -> doEnvFileConfigure(absolutePath, extension));

    }

    private void doEnvFileConfigure(String absolutePath, EnvFilePluginExtension extension) {
        List<String> envFiles = extension.getEnvFiles();
        if (Objects.isNull(envFiles) || envFiles.size() == 0) {
            envFiles = new ArrayList<>();
            envFiles.add("/mock/.env");
            envFiles.add("/mock/slowtest/.env");
        }

        Set<String> targetPaths = extension.getEnvFiles().stream().map(relativePath -> absolutePath + relativePath).collect(Collectors.toSet());

        FileReader fileReader = null;
        FileOutputStream fileOutputStream = null;

        try {
            for (String targetPath : targetPaths) {
                File file = new File(targetPath);
                if (!file.exists()) {
                    Files.createFile(FileSystems.getDefault().getPath(targetPath));
                }

                fileReader = new FileReader(targetPath);
                Properties properties = new Properties();
                properties.load(fileReader);
                properties.put("LOCAL_IP", getLocalIp());
                fileOutputStream = new FileOutputStream(targetPath);
                properties.store(fileOutputStream, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getLocalIp() throws SocketException {
        Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (allNetworkInterfaces.hasMoreElements()) {
            //get next network interface
            NetworkInterface networkInterface = allNetworkInterfaces.nextElement();
            //output interface's name
            if ("en0".equalsIgnoreCase(networkInterface.getDisplayName())) {
                Enumeration<InetAddress> allInetAddress = networkInterface.getInetAddresses();
                //check if there are more than one ip addresses
                //band to one network interface
                while (allInetAddress.hasMoreElements()) {
                    //get next ip address
                    InetAddress inetAddress = allInetAddress.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        return LOOPBACK_IP;
    }


}


