package ru.bozaro.techDb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.bozaro.techDb.hello.api.ApiApi;
import ru.bozaro.techDb.hello.model.Item;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloApiImpl implements ApiApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ResponseEntity<List<Item>> find(
            @RequestParam(value = "since", required = false) BigDecimal since,
            @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "limit", required = false, defaultValue = "100") BigDecimal limit
    ) {
        final StringBuilder sql = new StringBuilder("SELECT id, description, completed FROM tasks");
        final List<Object> args = new ArrayList<>();
        if (since != null) {
            sql.append(" WHERE id ");
            if (desc == Boolean.TRUE) {
                sql.append("< ?");
            } else {
                sql.append("> ?");
            }
            args.add(since);
        }
        sql.append(" ORDER BY id");
        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }
        sql.append(" LIMIT ?");
        args.add(limit);

        List<Item> items = jdbcTemplate.query(sql.toString(), args.toArray(new Object[args.size()]), HelloApiImpl::readItem);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<List<Item>> addMulti(
            @RequestBody List<Item> body
    ) {
        final List<Item> result = new ArrayList<>(body.size());
        if (!body.isEmpty()) {
            final List<Long> ids = jdbcTemplate.query("SELECT nextval('tasks_id_seq') FROM generate_series(1, ?)", new Object[]{body.size()}, (rs, rowNum) -> rs.getLong(1));
            jdbcTemplate.batchUpdate("INSERT INTO tasks (id, description, completed) VALUES (?, ?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Item item = body.get(i);
                    ps.setLong(1, ids.get(i));
                    ps.setString(2, item.getDescription());
                    ps.setBoolean(3, item.getCompleted());

                    Item inserted = new Item();
                    inserted.setId(ids.get(i));
                    inserted.setDescription(item.getDescription());
                    inserted.setCompleted(item.getCompleted());
                    result.add(inserted);
                }

                @Override
                public int getBatchSize() {
                    return body.size();
                }
            });
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Transactional
    @Override
    public ResponseEntity<Item> getOne(
            @PathVariable("id") BigDecimal id
    ) {
        List<Item> items = jdbcTemplate.query("SELECT id, description, completed FROM tasks WHERE id = ?", new Object[]{id}, HelloApiImpl::readItem);
        if (items.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(items.get(0), HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<Void> destroyOne(
            @PathVariable("id") BigDecimal id
    ) {
        int rowsAffected = jdbcTemplate.execute("DELETE FROM tasks WHERE id = ?", (PreparedStatementCallback<Integer>) ps -> {
            ps.setBigDecimal(1, id);
            return ps.executeUpdate();
        });
        if (rowsAffected == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Item> updateOne(
            @PathVariable("id") BigDecimal id,
            @RequestBody Item body
    ) {
        int rowsAffected = jdbcTemplate.update("UPDATE tasks SET description = ?, completed = ? WHERE id = ?", body.getDescription(), body.getCompleted(), id);
        if (rowsAffected == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Item updated = new Item();
        updated.setId(id.longValue());
        updated.setDescription(body.getDescription());
        updated.setCompleted(body.getCompleted());
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    private static Item readItem(ResultSet rs, int rowNum) throws SQLException {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setDescription(rs.getString("description"));
        item.setCompleted(rs.getBoolean("completed"));
        return item;
    }
}
