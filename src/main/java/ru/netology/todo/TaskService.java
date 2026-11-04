package ru.netology.todo;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public List<Task> findAll() {
        return tasks.values().stream().sorted(Comparator.comparingLong(Task::id)).toList();
    }

    public Task create(TaskRequest request) {
        long id = ids.incrementAndGet();
        Task task = new Task(id, request.title().trim(), Boolean.TRUE.equals(request.completed()), Instant.now());
        tasks.put(id, task);
        return task;
    }

    public Task update(long id, TaskRequest request) {
        Task old = tasks.get(id);
        if (old == null) throw new TaskNotFoundException(id);
        Task updated = new Task(id, request.title().trim(),
                request.completed() == null ? old.completed() : request.completed(), old.createdAt());
        tasks.put(id, updated);
        return updated;
    }

    public void delete(long id) {
        if (tasks.remove(id) == null) throw new TaskNotFoundException(id);
    }
}
