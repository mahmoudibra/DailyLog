package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.ReviewsViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class ReviewsComponent(@Component val parent: DatabaseComponent) {
    abstract val reviewsViewModel: ReviewsViewModel

    companion object {
        val instance: ReviewsComponent by lazy {
            ReviewsComponent::class.create(DatabaseComponent.instance)
        }
    }
}
