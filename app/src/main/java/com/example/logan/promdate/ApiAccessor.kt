package com.example.logan.promdate

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val BASE_URL = "http://ec2-35-183-247-114.ca-central-1.compute.amazonaws.com"

//TODO: https://stackoverflow.com/questions/35693680/how-to-send-form-data-in-retrofit2-android/35698175

//Requests that can be sent to the api
interface ServerInterface {
    //login
    @POST("php/authenticate.php")
    @FormUrlEncoded
    fun login(@Field("email") email: String,
              @Field("password") password: String): Call<DefaultResponse>

    //register
    @POST("php/register.php")
    @FormUrlEncoded
    fun register(@Field("email") email: String,
                 @Field("password") password: String,
                 @Field("password-retype") checkPassword: String,
                 @Field("first-name") firstName: String,
                 @Field("last-name") lastName: String,
                 @Field("school-id") schoolId: Int,
                 @Field("gender") gender: String,
                 @Field("grade") grade: Int): Call<DefaultResponse>
/*    //list of collections
    @GET("custom_collections.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6")
    fun loadAllCollections(): Call<CollectionsList>
    //specific collection
    @GET("collects.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6")
    fun loadCollectionProducts(@Query("collection_id") id: Long): Call<ProductIdList>
    //products
    @GET("products.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6")
    fun loadProducts(@Query("ids") id: String): Call<ProductList>*/
}

//initializes the standard api accessor that is reused throughout the code
class APIAccessor {
    var apiService: ServerInterface
    init {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ServerInterface::class.java)
    }
}