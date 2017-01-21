package ru.bozaro.techDb.service;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import ru.bozaro.techDb.hello.api.ApiApi;

import java.math.BigDecimal;

@Controller
public class HelloApiImpl implements ApiApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ResponseEntity<Void> destroyOne(@ApiParam(value = "", required = true) @PathVariable("id") BigDecimal id) {
        int rowsAffected = jdbcTemplate.execute("DELETE FROM tasks WHERE id = ?", (PreparedStatementCallback<Integer>) ps -> {
            ps.setBigDecimal(1, id);
            ps.executeUpdate();
            return ps.executeUpdate();
        });
        if (rowsAffected == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
