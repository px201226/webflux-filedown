package com.example.webfluxfiledown.controller;

import static org.apache.poi.util.IOUtils.closeQuietly;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.webfluxfiledown.model.User;
import com.example.webfluxfiledown.service.CsvWriterService;
import com.example.webfluxfiledown.util.ByteArrayInOutStream;
import com.sun.istack.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder.In;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.reactivestreams.Publisher;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
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
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
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

		return Flux.create(emitter -> {

			stream.forEach(i -> {
				emitter.next(String.valueOf(i));
			});

			emitter.complete();
		});

	}

	@GetMapping(value = "/4", produces = "application/octet-stream")
	public Mono<Void> aa2a(ServerHttpResponse response) throws IOException {
		final Stream<Integer> stream = Stream.iterate(0, i -> i + 1).limit(1000);

		final String repeat = "a".repeat(500);

		String fileName = String.format("%s.csv", RandomStringUtils.randomAlphabetic(10));
		response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
		response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

		final Flux<DataBuffer> next = Flux.<DataBuffer>create(emitter -> {

					stream
							.map(i -> String.format("value,%s\n", i).getBytes(StandardCharsets.UTF_8))
							.forEach(bytes -> {
								try {
									final DefaultDataBuffer dataBuffer = new DefaultDataBufferFactory().allocateBuffer();
									final OutputStream outputStream = dataBuffer.asOutputStream();
									outputStream.write(bytes);
									outputStream.flush();
									emitter.next(dataBuffer);
									Thread.sleep(10L);
								} catch (IOException | InterruptedException e) {
									e.printStackTrace();
								}
							});

					emitter.complete();
				}
		);

		final Mono<Void> voidMono = response.writeAndFlushWith(next.map(dataBuffer -> {
			return Mono.just(dataBuffer).doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
		}));

		return voidMono;
	}

//
//	private Mono<ServerResponse> writeToServerResponse(@NotNull String tag) {
//
//		return ServerResponse.ok()
//				.contentType(MediaType.APPLICATION_OCTET_STREAM)
//				.body(Flux.<DataBuffer>create((FluxSink<DataBuffer> emitter) -> {
//					// for a really big blob I want to read it in chunks, so that my server doesn't use too much memory
//					final int tagChunkSize;
//					final int blobSize;
//					for(int i = 0; i < blobSize; i+= tagChunkSize) {
//						// new DataBuffer that is written to, then emitted later
//						DefaultDataBuffer dataBuffer = new DefaultDataBufferFactory().allocateBuffer();
//						try (OutputStream outputStream = dataBuffer.asOutputStream()) {
//							// write to the outputstream of DataBuffer
//							tag.BlobReadPartial(outputStream, i, tagChunkSize, FPLibraryConstants.FP_OPTION_DEFAULT_OPTIONS);
//							// don't know if flushing is strictly neccessary
//							outputStream.flush();
//						} catch (IOException e) {
//							emitter.error(e);
//						}
//						emitter.next(dataBuffer);
//					}
//					// if blob is finished, send "complete" to my flux of DataBuffers
//					emitter.complete();
//				}, OverflowStrategy.BUFFER).publishOn(Schedulers.newElastic("centera")), DataBuffer.class);
//
//	}
}
