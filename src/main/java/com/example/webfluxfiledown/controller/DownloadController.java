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
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class DownloadController {

	private final CsvWriterService csvWriterService;

	public DownloadController(CsvWriterService csvWriterService) {
		this.csvWriterService = csvWriterService;
	}

	@GetMapping(value = "/download")
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
    File file = resource.getFile();
	  final Mono<Resource> just = Mono.just(resource);
    return just;
	}


	@GetMapping("/3")
	public ResponseEntity aaa(ServerHttpResponse response) throws IOException {

		InputStreamResource resource = new InputStreamResource(new FileInputStream(new File("./mock.csv")));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"filename.csv\"")
				.contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(resource);
	}

}
