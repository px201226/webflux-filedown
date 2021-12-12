package com.example.webfluxfiledown.service;

import com.example.webfluxfiledown.model.User;
import com.example.webfluxfiledown.util.ByteArrayInOutStream;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
public class CsvWriterService {

    public Mono<ByteArrayInputStream> generateCsv(){
        String[] columns = {"Name", "Email", "Country"};


        final List<User> users = IntStream.rangeClosed(0, 1000000)
            .mapToObj(i -> new User(String.valueOf(i), "px" + i + "@naver.com", "1"))
            .collect(Collectors.toList());

        System.out.println("dd");
        return Mono.fromCallable(() -> {
            try {
                ByteArrayInOutStream stream = new ByteArrayInOutStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
                CSVWriter writer = new CSVWriter(streamWriter);

                ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
                mappingStrategy.setType(User.class);
                mappingStrategy.setColumnMapping(columns);
                writer.writeNext(columns);

                StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withMappingStrategy(mappingStrategy)
                    .withSeparator(',')
                    .build();

                beanToCsv.write(users);
                streamWriter.flush();
                return stream.getInputStream();
            }
            catch (CsvException | IOException e) {
                throw new RuntimeException(e);
            }

        }).subscribeOn(Schedulers.boundedElastic());

    }
}
