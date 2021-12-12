package com.example.webfluxfiledown.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.webfluxfiledown.model.User;
import com.example.webfluxfiledown.service.CsvWriterService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.ResourceHttpMessageWriter;
import org.springframework.http.codec.ServerSentEventHttpMessageWriter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@RestController
@RequestMapping("/api")
public class DownloadController {

	private final CsvWriterService csvWriterService;

	public DownloadController(CsvWriterService csvWriterService) {
		this.csvWriterService = csvWriterService;
	}

	@GetMapping(value = "/1")
	@ResponseBody
	public ResponseEntity downloadCsv() throws MalformedURLException {

		String fileName = String.format("%s.csv", RandomStringUtils.randomAlphabetic(10));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.body(new FileUrlResource("./mock.csv"));
	}


  @GetMapping("/2")
  public Mono<Resource> downloadByWriteWith(ServerHttpResponse response) throws IOException {

	  String fileName = String.format("%s.csv", RandomStringUtils.randomAlphabetic(10));
    response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

    Resource resource = new FileUrlResource("./mock.csv");
	  final Mono<Resource> just = Mono.just(resource);
    return just;
	}


	@GetMapping("/3")
	public Flux<String> aaa(ServerHttpResponse response) throws IOException {
		final Stream<Integer> stream = Stream.iterate(0, i -> i + 1).limit(100);

		final Flux<String> value = Flux.fromStream(stream) // Limit 제외
				.zipWith(Flux.interval(Duration.ofMillis(100)))
				.map(i -> String.format("value,%d\n", i.getT1()));

		String fileName = String.format("%s.csv", RandomStringUtils.randomAlphabetic(10));
		response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
		response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

		return value;
	}

	@GetMapping("/4")
	public Flux aa2a(ServerHttpResponse response) throws IOException {
		Stream<Integer> stream = Stream.iterate(0, i -> i + 1);
		return Flux.fromStream(stream.limit(2)).zipWith(Flux.interval(Duration.ofSeconds(1)))
				.map(tuple -> Collections.singletonMap("value", tuple.getT1() /* 튜플의 첫 번째 요소 = Stream<Integer> 요소 */));
	}
}
