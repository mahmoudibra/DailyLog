package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.SignInViewModel
import com.booking.worktracker.presentation.viewmodels.SignUpViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class AuthComponent(@Component val parent: DatabaseComponent) {

    abstract val signInViewModel: SignInViewModel

    abstract val signUpViewModel: SignUpViewModel

    companion object {
        val instance: AuthComponent by lazy {
            AuthComponent::class.create(DatabaseComponent.instance)
        }
    }
}
