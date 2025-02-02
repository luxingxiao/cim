package com.crossoverjie.cim.server;

import com.crossoverjie.cim.server.config.AppConfiguration;
import com.crossoverjie.cim.server.kit.RegistryZK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

/**
 * @author crossoverJie
 */
@SpringBootApplication
public class CIMServerApplication implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(CIMServerApplication.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Value("${server.port}")
    private int httpPort;

    @Value("${im.message.topic}")
    private String topic;

    public static void main(String[] args) {
        SpringApplication.run(CIMServerApplication.class, args);
        LOGGER.info("Start cim server success!!!");
    }

    @Override
    public void run(String... args) throws Exception {
        Thread thread = new Thread(new RegistryZK(appConfiguration.getCimServerIp(), appConfiguration.getCimServerPort(), httpPort, topic));
        thread.setName("registry-zk");
        thread.start();
    }
}