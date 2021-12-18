package com.example.webfluxfiledown.controller;


import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@EnableWebFlux
public class Config  implements WebFluxConfigurer {

	@Override public void configureHttpMessageCodecs(final ServerCodecConfigurer configurer) {
		configurer.defaultCodecs().maxInMemorySize(100);
	}
}
