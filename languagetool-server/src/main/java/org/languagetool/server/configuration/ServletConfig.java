package org.languagetool.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class ServletConfig {
  @Bean
  public EmbeddedServletContainerCustomizer containerCustomizer() {
    return (container -> {
      container.setPort(8012);
      try {
        container.setAddress(InetAddress.getByName("localhost"));
      }
      catch (UnknownHostException e) {
        log.error("error!", e);
      }
    });
  }
}
