package org.producer.domain;

public record LibraryEvent(
        int libraryEventId,
        LibraryEventType libraryEventType,
        Book book
) {
}
