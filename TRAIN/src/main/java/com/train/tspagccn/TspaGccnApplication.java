package com.train.tspagccn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan(basePackages = "com.train.tspagccn.controller")
public class TspaGccnApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(TspaGccnApplication.class);
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(TspaGccnApplication.class, args);
  }
}
