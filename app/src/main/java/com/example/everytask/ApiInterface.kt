package com.example.everytask

import com.example.everytask.models.Default
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {

    @POST("register_user")
    fun registerUser(
        @Query("username") name: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("is_teacher") is_teacher: Boolean?
    ): Call<Default>

    @POST("login_user")
    fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<Default>

    @POST("task/get")
    fun getTasks(
        @Query("token") token: String
    ): Call<Default>

    @POST("task/add")
    fun addTask(
        @Query("token") token: String,
        @Query("title") title: String,
        @Query("description") description: String,
        @Query("due_time") due_time: String?,
        @Query("create_time") create_time: String?,
        @Query("note") note: String,
        @Query("group_id") group_id: Int?,
    ): Call<Default>

    //delete task
    @POST("task/remove")
    fun deleteTask(
        @Query("token") token: String,
        @Query("task_id") task_id: String
    ): Call<Default>
}