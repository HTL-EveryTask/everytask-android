package com.example.everytask

import com.example.everytask.models.call.*
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
        @Body body: Map<String, String>
    ): Call<Default>

    @GET("user")
    fun getUserData(
        @Header("Authorization") token: String
    ): Call<Default>

    @PATCH("user/picture")
    fun changeProfilePicture(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Default>

    @POST("verification/send")
    fun sendVerificationMail(
        @Body body: Map<String, String>
    ): Call<Default>

    @HTTP(method = "DELETE", path = "user", hasBody = true)
    fun deleteAccount(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Default>

    @PATCH("password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordInfo: PasswordInfo
    ): Call<Default>

    @POST("password")
    fun sendPasswordResetMail(
        @Body body: Map<String, String>
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
        @Body taskInfo: TaskInfo
    ): Call<Default>

    @HTTP(method = "DELETE", path = "task/{id}")
    fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @PATCH("task/{id}/done")
    fun toggleDone(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, Boolean>
    ): Call<Default>

    @PATCH("task/{id}")
    fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body taskInfo: TaskInfo
    ): Call<Default>

    // Appointments --------------------------------------------------------------------------------------------

    @GET("appointment")
    fun getAppointments(
        @Header("Authorization") token: String
    ): Call<Default>

    @GET("appointment/{id}")
    fun getSingleAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @POST("appointment")
    fun addAppointment(
        @Header("Authorization") token: String,
        @Body appointmentInfo: AppointmentInfo
    ): Call<Default>

    @HTTP(method = "DELETE", path = "appointment/{id}")
    fun deleteAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Default>

    @PATCH("appointment/{id}")
    fun updateAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body appointmentInfo: AppointmentInfo
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
        @Body body: GroupInfo
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

    @PATCH("group/{id}")
    fun updateGroup(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: GroupInfo
    ): Call<Default>

    @HTTP(method = "DELETE", path = "group/{id}/key")
    fun lockGroup(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<Default>

    @PUT("group/{id}/admin")
    fun addAdmin(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, Int>
    ): Call<Default>

    @HTTP(method = "DELETE", path = "group/{id}/admin", hasBody = true)
    fun removeAdmin(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, Int>
    ): Call<Default>

    @HTTP(method = "DELETE", path = "group/{id}/kick", hasBody = true)
    fun kickUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, Int>
    ): Call<Default>

    // CONNECTIONS --------------------------------------------------------------------------------------------

    @POST("untis")
    fun loginUntis(
        @Header("Authorization") token: String,
        @Body body: UntisInfo
    ): Call<Default>
}