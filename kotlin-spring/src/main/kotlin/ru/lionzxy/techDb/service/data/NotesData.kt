package ru.lionzxy.techDb.service.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.lionzxy.techDb.hello.model.Item
import ru.lionzxy.techDb.service.models.NotFoundNoteException
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Statement

@Component
class NotesData(private val template: JdbcTemplate) {
    companion object {
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_COMPLETED = "completed"
        private const val COLUMN_ID = "id"

        val NOTEMAPPER = RowMapper<Item> { row, _ ->
            val item = Item()
            item.id = row.getLong(COLUMN_ID)
            item.description = row.getString(COLUMN_DESCRIPTION)
            item.completed = row.getBoolean(COLUMN_COMPLETED)
            return@RowMapper item
        }
    }

    fun get(since: BigDecimal?, desc: Boolean, limit: BigDecimal?): List<Item> {
        var sql = "SELECT * FROM notes "
        val argsObject = ArrayList<Any>()

        if (since != null) {
            sql += if (desc) {
                "WHERE id < ? "
            } else {
                "WHERE id > ? "
            }
            argsObject.add(since)
        }

        sql += if (desc) {
            "ORDER BY id DESC "
        } else {
            "ORDER BY id ASC "
        }

        if (limit != null) {
            sql += "LIMIT ?"
            argsObject.add(limit)
        }

        return template.query(sql, argsObject.toArray(), NOTEMAPPER)
    }

    fun getById(id: BigDecimal): Item {
        return template.queryForObject("SELECT * FROM notes WHERE id = ?;", NOTEMAPPER, id)
    }

    fun put(notes: List<Item>): List<Item> {
        val connection = template.dataSource.connection
        try {
            connection.autoCommit = false
            return putThrowables(connection, notes)
        } finally {
            connection.autoCommit = true
            connection.close()
        }
    }

    private fun putThrowables(connection: Connection, notes: List<Item>): List<Item> {
        val idsResultSet = template.queryForRowSet("SELECT nextval('notes_id_seq') FROM generate_series(1, ?);", notes.size)

        val ps = connection.prepareStatement("INSERT INTO notes(id, description, completed) VALUES (?, ?, ?);", Statement.NO_GENERATED_KEYS)

        notes.forEach({
            ps.apply {
                idsResultSet.next()
                it.id = idsResultSet.getLong(1)
                setLong(1, it.id)
                setString(2, it.description)
                setBoolean(3, it.completed)
                addBatch()
            }
        })

        try {
            val insertCount = ps.executeBatch()
            if (insertCount.contains(0)) {
                throw RuntimeException("Ошибка при заполнение")
            }
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        }

        return notes
    }

    fun editNote(note: Item): Item {
        return template.queryForObject("UPDATE notes SET (description, completed) = (coalesce(?, description), coalesce(?, completed)) WHERE id = ? RETURNING *;",
                NOTEMAPPER,
                note.description,
                note.completed,
                note.id)
    }

    fun removeNote(id: BigDecimal) {
        val count = template.update("DELETE FROM notes WHERE id = ?;", id)
        if (count == 0) {
            throw NotFoundNoteException()
        }
    }
}