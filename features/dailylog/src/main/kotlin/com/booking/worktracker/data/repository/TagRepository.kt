package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.TagLocalDataSource
import com.booking.worktracker.data.models.Tag
import me.tatarka.inject.annotations.Inject
import com.booking.worktracker.di.Singleton

@Inject
@Singleton
class TagRepository(private val localDataSource: TagLocalDataSource) {

    fun getAllTags(): List<Tag> {
        return localDataSource.getAllTags()
    }

    fun createTag(name: String, color: String?): Tag {
        return localDataSource.createTag(name, color)
    }
}
