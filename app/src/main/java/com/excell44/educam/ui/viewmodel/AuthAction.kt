package com.excell44.educam.ui.viewmodel

import com.excell44.educam.ui.base.UiAction

sealed interface AuthAction : UiAction {
    data class Login(val email: String, val pass: String) : AuthAction
    data class Register(val email: String, val pass: String, val name: String, val grade: String) : AuthAction
    data class RegisterFull(
        val pseudo: String,
        val pass: String,
        val fullName: String,
        val gradeLevel: String,
        val school: String,
        val city: String,
        val neighborhood: String,
        val parentName: String?,
        val parentPhone: String?,
        val relation: String?,
        val promoCode: String?
    ) : AuthAction
    data class RegisterOffline(
        val pseudo: String,
        val pass: String,
        val fullName: String,
        val gradeLevel: String
    ) : AuthAction
    object Logout : AuthAction
    object GuestMode : AuthAction
    object ClearError : AuthAction
}
