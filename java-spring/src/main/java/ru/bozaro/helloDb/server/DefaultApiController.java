package ru.bozaro.helloDb.server;

import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import ru.bozaro.techDb.hello.api.DefaultApi;
import ru.bozaro.techDb.hello.model.Item;

import java.math.BigDecimal;
import java.util.List;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2017-01-20T16:14:48.641+04:00")

@Controller
public class DefaultApiController implements DefaultApi {
    @Override
    public ResponseEntity<List<Item>> find(@ApiParam(value = "Идентификатор записи, с которой будет возвращаться записи. ") @RequestParam(value = "since", required = false) BigDecimal since, @ApiParam(value = "Порядок сортировки (asc - по возрастанию, desc - по убыванию). ", allowableValues = "{values=[asc, desc], enumVars=[{name=ASC, value=\"asc\"}, {name=DESC, value=\"desc\"}]}", defaultValue = "asc") @RequestParam(value = "order", required = false, defaultValue = "asc") String order, @ApiParam(value = "Максимальное кол-во возвращаемых записей.", defaultValue = "100") @RequestParam(value = "limit", required = false, defaultValue = "100") BigDecimal limit) {
        return null;
    }
}
