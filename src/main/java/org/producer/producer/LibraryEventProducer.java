package org.producer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.producer.domain.LibraryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventProducer {
    @Value("${spring.kafka.topic}")
    private String topic;
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public CompletableFuture<SendResult<Integer, String>> sendEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = mapper.writeValueAsString(libraryEvent);

        return kafkaTemplate.send(topic, key, value)
                .whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        handleError(key, value, throwable);
                    } else {
                        handleSuccess(key, value, sendResult);
                    }
                });
    }

    public CompletableFuture<SendResult<Integer, String>> sendEventV2(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = mapper.writeValueAsString(libraryEvent);

        var record = buildProducerRecord(key, value);

        return kafkaTemplate.send(record)
                .whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        handleError(key, value, throwable);
                    } else {
                        handleSuccess(key, value, sendResult);
                    }
                });
    }

    private ProducerRecord<Integer,String> buildProducerRecord(int key, String value) {
        List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        return new ProducerRecord<>(topic,null,key,value,headers);
    }

    private void handleSuccess(int key, String value, SendResult<Integer, String> sendResult) {
        log.info("Successfully send event key : {}, value : {}, partition : {}", key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleError(int key, String value, Throwable throwable) {
        log.info("Could not send event key : {}, Value : {}, error : {}", key, value, throwable.getMessage());
    }
}
