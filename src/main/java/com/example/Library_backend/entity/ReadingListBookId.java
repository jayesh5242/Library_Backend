package com.example.Library_backend.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReadingListBookId implements Serializable {

    private Long readingList;
    private Long book;
}
