package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.repository.TagRepository
import me.tatarka.inject.annotations.Inject

@Inject
class GetAllTagsUseCase(private val tagRepository: TagRepository) {
    operator fun invoke(): List<Tag> = tagRepository.getAllTags()
}

@Inject
class CreateTagUseCase(private val tagRepository: TagRepository) {
    operator fun invoke(name: String, color: String?): Result<Tag> {
        return try {
            require(name.isNotBlank()) { "Tag name cannot be blank" }
            Result.success(tagRepository.createTag(name, color))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
