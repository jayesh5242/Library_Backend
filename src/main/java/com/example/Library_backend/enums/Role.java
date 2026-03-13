package com.example.Library_backend.enums;

public enum Role {
    STUDENT,
    FACULTY,
    LIBRARIAN,
    SUPER_ADMIN
}

//        **What is an Enum?**
//        > An Enum is a fixed list of values. Role can ONLY be one of these 4 values — nothing else.
//        This prevents mistakes like someone saving "studdent" (typo) in the database.
