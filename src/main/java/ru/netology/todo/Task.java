package ru.netology.todo;

import java.time.Instant;

public record Task(long id, String title, boolean completed, Instant createdAt) {}
