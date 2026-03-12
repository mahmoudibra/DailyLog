package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.TagLocalDataSource
import com.booking.worktracker.data.models.Tag

class TagRepository(
    private val localDataSource: TagLocalDataSource
) {

    fun getAllTags(): List<Tag> {
        return localDataSource.getAllTags()
    }

    fun createTag(name: String, color: String?): Tag {
        return localDataSource.createTag(name, color)
    }
}
