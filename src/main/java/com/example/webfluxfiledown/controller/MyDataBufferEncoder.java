package com.example.webfluxfiledown.controller;

import java.awt.image.DataBuffer;
import java.util.List;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMessage;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

public class MyDataBufferEncoder implements HttpMessageEncoder<DataBuffer> {

	@Override public List<MediaType> getStreamingMediaTypes() {
		return null;
	}

	@Override public boolean canEncode(final ResolvableType elementType, final MimeType mimeType) {
		return false;
	}

	@Override
	public Flux<org.springframework.core.io.buffer.DataBuffer> encode(final Publisher<? extends DataBuffer> inputStream, final DataBufferFactory bufferFactory,
			final ResolvableType elementType, final MimeType mimeType, final Map<String, Object> hints) {
		return null;
	}

	@Override public List<MimeType> getEncodableMimeTypes() {
		return null;
	}
}
