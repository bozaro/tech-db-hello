package ru.bozaro.techDb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import ru.bozaro.techDb.hello.api.ApiApi;

@Controller
public class HelloApiImpl implements ApiApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

}
