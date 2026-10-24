package com.zagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序（启动横幅见 banner.txt）
 *
 * @author zagent
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ZAgentApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ZAgentApplication.class, args);
    }
}
