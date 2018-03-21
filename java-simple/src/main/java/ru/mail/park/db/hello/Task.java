package ru.mail.park.db.hello;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

    private final Long id;
    private final String description;
    private final Boolean completed;

    @JsonCreator
    public Task(@JsonProperty("id") Long id, @JsonProperty("description") String description, @JsonProperty("completed") Boolean completed) {
        this.id = id;
        this.description = description;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getCompleted() {
        return completed;
    }

}
