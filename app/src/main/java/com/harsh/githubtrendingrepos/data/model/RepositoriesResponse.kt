package com.harsh.githubtrendingrepos.data.model

import com.google.gson.annotations.SerializedName

data class RepositoriesResponse(

    @SerializedName("total_count")
    val total: Int = 0,

    @SerializedName("items")
    val items: MutableList<Repo>,

    val nextPage: Int? = null

)
