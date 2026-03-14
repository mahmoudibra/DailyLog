package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.Tag
import me.tatarka.inject.annotations.Inject
import com.booking.worktracker.di.Singleton

@Inject
@Singleton
class TagLocalDataSource(db: DailyTrackerDatabase) {

    private val tagsQueries = db.tagsQueries
    private val dailyLogsQueries = db.dailyLogsQueries

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
