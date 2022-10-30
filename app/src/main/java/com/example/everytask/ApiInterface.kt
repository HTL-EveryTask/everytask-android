package com.example.everytask

import com.example.everytask.models.Login
import com.example.everytask.models.TestItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("posts")
    fun getData(): Call<List<TestItem>>

    @GET("register_user")
    fun registerUser(
        @Query("username") name: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("is_teacher") is_teacher: Boolean
    ): Call<Login>

    @GET("login_user")
    fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<Login>
}