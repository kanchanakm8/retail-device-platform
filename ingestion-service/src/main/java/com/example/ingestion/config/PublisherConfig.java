package com.example.ingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.example.ingestion.publisher.EventPublisher;
import com.example.ingestion.publisher.RestEventPublisher;
import com.example.ingestion.repository.DlqEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class PublisherConfig {

	private static final Logger log = LoggerFactory.getLogger(PublisherConfig.class);

	@Value("${inventory.service.process.url}")
	private String inventoryServiceProcessUrl;

	@Bean
	public RestTemplate restTemplate() {
		log.info("RestTemplate bean initialized");
		return new RestTemplate();
	}

	@Bean
	public EventPublisher eventPublisher(RestTemplate restTemplate, DlqEventRepository dlqRepository,
			ObjectMapper objectMapper) {
		log.info("EventPublisher bean initialized (using REST)");
		return new RestEventPublisher(restTemplate, dlqRepository, inventoryServiceProcessUrl, objectMapper);
	}

//	@Bean
//	public EventPublisher eventPublisher(RestEventPublisher restEventPublisher) {
//		return restEventPublisher;
//	}
}