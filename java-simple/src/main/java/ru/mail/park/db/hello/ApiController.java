package ru.mail.park.db.hello;

import static org.springframework.http.HttpStatus.CREATED;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

	private final JdbcTemplate jdbcTemplate;

	public ApiController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostMapping
	public ResponseEntity<List<Task>> post(@RequestBody List<Task> body) {
		final List<Task> result = new ArrayList<>(body.size());
		if (!body.isEmpty()) {
			final List<Long> ids = jdbcTemplate
					.queryForList("SELECT nextval('tasks_id_seq') FROM generate_series(1, ?)", Long.class, body.size());

			jdbcTemplate.batchUpdate("INSERT INTO tasks (id, description, completed) VALUES (?, ?, ?)",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int i) throws SQLException {
							Task task = body.get(i);
							ps.setLong(1, ids.get(i));
							ps.setString(2, task.getDescription());
							ps.setBoolean(3, task.getCompleted());

							result.add(new Task(ids.get(i), task.getDescription(), task.getCompleted()));
						}

						@Override
						public int getBatchSize() {
							return body.size();
						}
					});
		}
		return ResponseEntity.status(CREATED).body(result);

	}

	@GetMapping
	public List<Task> list(@RequestParam(required = false) Integer since,
			@RequestParam(defaultValue = "false") boolean desc,
			@RequestParam(defaultValue = "100") int limit) {

		StringBuilder sql = new StringBuilder("select * from tasks");
		if (since != null) {
			sql.append(" where id ");
			if (desc == Boolean.TRUE) {
				sql.append("< ?");
			} else {
				sql.append("> ?");
			}
		}
		sql.append(" order by id");
		if (desc == Boolean.TRUE) {
			sql.append(" desc");
		}
		sql.append(" limit ?");

		if (since != null) {
			return jdbcTemplate.query(sql.toString(), ApiController::readTask, since, limit);
		} else {
			return jdbcTemplate.query(sql.toString(), ApiController::readTask, limit);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Task> get(@PathVariable long id) {
		List<Task> result = jdbcTemplate.query("select * from tasks where id = ?", ApiController::readTask, id);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(result.get(0));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		int rows = jdbcTemplate.update("delete from tasks where id = ?", id);
		if (rows == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Task> update(
			@PathVariable("id") long id,
			@RequestBody Task task) {
		int rows = jdbcTemplate.update("update tasks set description = ?, completed = ? where id = ?",
				task.getDescription(), task.getCompleted(), id);
		if (rows == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new Task(id, task.getDescription(), task.getCompleted()));

	}

	private static Task readTask(ResultSet rs, int rowNum) throws SQLException {
		return new Task(rs.getLong("id"), rs.getString("description"), rs.getBoolean("completed"));
	}

}
