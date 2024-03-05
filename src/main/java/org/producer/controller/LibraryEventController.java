package org.producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.producer.domain.LibraryEvent;
import org.producer.producer.LibraryEventProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class LibraryEventController {
    private final LibraryEventProducer libraryEventProducer;
    @PostMapping("/v1/post-event")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent){

        log.info("libraryEvent : {}",libraryEvent);
        try {
            var future = libraryEventProducer.sendEventV2(libraryEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @GetMapping("/v1/greeting-event/{name}")
    public ResponseEntity<?> greeting(@PathVariable String name){
        return ResponseEntity.ok(Collections.singletonMap("message",STR."Hello, \{name}!"));
    }
}
