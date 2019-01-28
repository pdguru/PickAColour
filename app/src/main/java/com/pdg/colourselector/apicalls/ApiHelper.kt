package com.pdg.colourselector.apicalls

import com.pdg.colourselector.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiHelper {

    @POST("login")
    fun login(@Body loginTask: UserLoginCreds): Call<AuthToken>

    @GET()
    fun getColour(
        @Url endpoint: String,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String
    ): Call<ServerResponse>

    @PUT()
    fun saveColour(
        @Url endpoint: String,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String,
        @Body data: PayloadForRequest
    ): Call<ServerResponse>

    @POST("storage")
    fun createStorage(
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String
    ): Call<ServerResponse>

    @DELETE()
    fun delete(
        @Url endpoint: String,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String
    ): Call<String>

}
