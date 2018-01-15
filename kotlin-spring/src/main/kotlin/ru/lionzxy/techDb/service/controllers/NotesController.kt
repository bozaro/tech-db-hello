package ru.lionzxy.techDb.service.controllers

import javassist.tools.web.BadHttpRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.stereotype.Controller
import ru.lionzxy.techDb.hello.api.ApiApi
import ru.lionzxy.techDb.hello.model.Item
import ru.lionzxy.techDb.service.data.NotesData
import ru.lionzxy.techDb.service.models.NotFoundNoteException
import java.math.BigDecimal


@Controller
class NotesController : ApiApi {

    @Autowired
    private lateinit var notesData: NotesData

    override fun addMulti(@RequestBody body: MutableList<Item>?): ResponseEntity<MutableList<Item>> {
        var list = listOf<Item>()
        if (body != null) {
            list = notesData.put(body)
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(list.toMutableList())
    }

    override fun destroyOne(@PathVariable("id") id: BigDecimal?): ResponseEntity<Void> {
        return try {
            notesData.removeNote(id ?: throw BadHttpRequest())
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: NotFoundNoteException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    override fun find(@RequestParam(value = "since", required = false) since: BigDecimal?,
                      @RequestParam(value = "desc", required = false) desc: Boolean?,
                      @RequestParam(value = "limit", required = false, defaultValue = "100") limit: BigDecimal?): ResponseEntity<MutableList<Item>> {
        val list = try {
            notesData.get(since, desc ?: false, limit)
        } catch (e: EmptyResultDataAccessException) {
            listOf<Item>()
        }

        return ResponseEntity.ok(list.toMutableList())
    }

    override fun getOne(@PathVariable("id") id: BigDecimal?): ResponseEntity<Item> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(notesData.getById(id ?: throw BadHttpRequest()))
        } catch (e: EmptyResultDataAccessException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    override fun updateOne(@PathVariable("id") id: BigDecimal?,
                           @RequestBody body: Item?): ResponseEntity<Item> {
        body?.id = (id ?: throw BadHttpRequest()).toLong()
        if (body == null || body.id == -1L) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        return try {
            ResponseEntity.ok(notesData.editNote(body))
        } catch (e: EmptyResultDataAccessException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

}