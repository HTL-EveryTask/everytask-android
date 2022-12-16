package com.example.everytask.models.response.groups

data class GroupUser(
    val id: Int,
    val is_admin: Boolean,
    val is_teacher: Boolean,
    val username: String
): java.io.Serializable