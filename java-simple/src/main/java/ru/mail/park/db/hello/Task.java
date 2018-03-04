package ru.mail.park.db.hello;

public class Task {

	private final Long id;
	private final String description;
	private final Boolean completed;

	public Task(Long id, String description, Boolean completed) {
		super();
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
