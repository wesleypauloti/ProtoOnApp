package com.example.login

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ViaCepService {
    @GET("{cep}/json/")
    fun getCepData(@Path("cep") cep: String): Call<CepResponse>
}