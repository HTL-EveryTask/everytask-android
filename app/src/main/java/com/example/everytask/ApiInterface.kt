package com.example.everytask

import com.example.everytask.models.Task
import com.example.everytask.models.call.LoginInfo
import com.example.everytask.models.call.RegisterInfo
import com.example.everytask.models.call.TaskInfo
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @PUT("register_user")
    fun registerUser(
        @Body registerInfo: RegisterInfo
    ): Call<Default>

    @POST("login_user")
    fun loginUser(
        @Body loginIngo: LoginInfo
    ): Call<Default>

    @GET("tasks")
    fun getTasks(
        @Header("Authorization") token: String
    ): Call<Default>

    @PUT("task")
    fun addTask(
        @Header("Authorization") token: String,
        @Body task: Task
    ): Call<Default>

    //in body only id of task as int
    @HTTP(method = "DELETE", path = "task/{id}", hasBody = true)
    fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @POST("token")
    fun verifyToken(
        @Header("Authorization") token: String
    ): Call<Default>

    @PATCH("task/{id}/done")
    fun toggleDone(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String,Boolean>
    ): Call<Default>

    @PATCH("task")
    fun updateTask(
        @Header("Authorization") token: String,
        @Body task: Task
    ): Call<Default>
}