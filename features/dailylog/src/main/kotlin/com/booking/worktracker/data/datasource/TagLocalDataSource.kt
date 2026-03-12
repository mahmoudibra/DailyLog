package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.Tag

class TagLocalDataSource {

    fun getAllTags(): List<Tag> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT id, name, color, created_at FROM tags ORDER BY name")
        val rs = stmt.executeQuery()

        val tags = mutableListOf<Tag>()
        while (rs.next()) {
            tags.add(
                Tag(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    color = rs.getString("color"),
                    createdAt = rs.getString("created_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return tags
    }

    fun createTag(name: String, color: String?): Tag {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "INSERT INTO tags (name, color) VALUES (?, ?)"
        )
        stmt.setString(1, name)
        stmt.setString(2, color)
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val tagId = idRs.getInt(1)
        idRs.close()

        // Read back
        val readStmt = conn.prepareStatement("SELECT id, name, color, created_at FROM tags WHERE id = ?")
        readStmt.setInt(1, tagId)
        val rs = readStmt.executeQuery()
        rs.next()
        val tag = Tag(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            color = rs.getString("color"),
            createdAt = rs.getString("created_at")
        )
        rs.close()
        readStmt.close()
        return tag
    }
}
