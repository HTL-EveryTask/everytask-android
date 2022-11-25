package com.example.everytask

import com.example.everytask.models.Task
import com.example.everytask.models.call.LoginInfo
import com.example.everytask.models.call.RegisterInfo
import com.example.everytask.models.call.TaskInfo
import com.example.everytask.models.call.UntisInfo
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    // USER --------------------------------------------------------------------------------------------

    @PUT("register_user")
    fun registerUser(
        @Body registerInfo: RegisterInfo
    ): Call<Default>

    @POST("login_user")
    fun loginUser(
        @Body loginIngo: LoginInfo
    ): Call<Default>

    @POST("token")
    fun verifyToken(
        @Header("Authorization") token: String
    ): Call<Default>

    @PATCH("user")
    fun changeUsername(
        @Header("Authorization") token: String,
        @Body username: String
    ): Call<Default>

    @GET("user")
    fun getUserData(
        @Header("Authorization") token: String
    ): Call<Default>

    @POST("verification/send")
    fun sendVerificationMail(
        @Body body: Map<String,String>
    ): Call<Default>

    // TASKS --------------------------------------------------------------------------------------------

    @GET("tasks")
    fun getTasks(
        @Header("Authorization") token: String
    ): Call<Default>

    @GET("task/{id}")
    fun getSingleTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @PUT("task")
    fun addTask(
        @Header("Authorization") token: String,
        @Body task: Task
    ): Call<Default>

    @HTTP(method = "DELETE", path = "task/{id}", hasBody = true)
    fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @PATCH("task/{id}/done")
    fun toggleDone(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String,Boolean>
    ): Call<Default>

    @PATCH("task/{id}")
    fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body task: Task
    ): Call<Default>

    // GROUPS --------------------------------------------------------------------------------------------

    @GET("groups")
    fun getGroups(
        @Header("Authorization") token: String
    ): Call<Default>

    @GET("group/{id}")
    fun getSingleGroup(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @PUT("group")
    fun addGroup(
        @Header("Authorization") token: String,
        @Body body: Map<String,String>
    ): Call<Default>

    @POST("group/{id}/invite")
    fun createInvite(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<Default>

    @POST("group/{id}/leave")
    fun leaveGroup(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<Default>

    // CONNECTIONS --------------------------------------------------------------------------------------------

    @POST("untis")
    fun loginUntis(
        @Header("Authorization") token: String,
        @Body body: UntisInfo
    ): Call<Default>
}