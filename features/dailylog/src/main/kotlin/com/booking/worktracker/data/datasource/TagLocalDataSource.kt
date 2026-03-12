package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyLogsQueries
import com.booking.worktracker.data.TagsQueries
import com.booking.worktracker.data.models.Tag

class TagLocalDataSource(
    private val tagsQueries: TagsQueries,
    private val dailyLogsQueries: DailyLogsQueries
) {

    fun getAllTags(): List<Tag> {
        return tagsQueries.getAllTags().executeAsList().map { row ->
            Tag(id = row.id.toInt(), name = row.name, color = row.color, createdAt = row.created_at)
        }
    }

    fun createTag(name: String, color: String?): Tag {
        val tagId = tagsQueries.transactionWithResult {
            tagsQueries.insertTag(name, color)
            dailyLogsQueries.lastInsertRowId().executeAsOne()
        }
        val row = tagsQueries.getTagById(tagId).executeAsOne()
        return Tag(id = row.id.toInt(), name = row.name, color = row.color, createdAt = row.created_at)
    }
}
