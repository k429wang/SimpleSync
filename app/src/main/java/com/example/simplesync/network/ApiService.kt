package com.example.simplesync.network

import com.example.simplesync.model.User

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("users")
    fun getAllUsers(): Call<List<User>>

    @POST("users")
    fun createUser(@Body user: User): Call<User>

    @GET("events/{username}")
    fun getUserByUsername(@Path("username") username: String): Call<User>
}
