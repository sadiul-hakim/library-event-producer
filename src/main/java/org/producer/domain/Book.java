package org.producer.domain;

public record Book(
        int bookId,
        String bookName,
        String bookAuthor
) {
}
