package com.campus.lostandfound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class CampusLostAndFoundApplication implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(CampusLostAndFoundApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("=========================================");
            System.out.println("数据库连接成功！");
            System.out.println("URL: " + connection.getMetaData().getURL());
            System.out.println("用户: " + connection.getMetaData().getUserName());
            System.out.println("=========================================");
        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }
}
