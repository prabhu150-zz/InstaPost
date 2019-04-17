package com.example.instapost.Model

class HashTag(
    var hashTagText: String
) {
    var posts = arrayListOf<Post>()

    fun addPost(currentPost: Post) {
        posts.add(currentPost)
    }

    fun getPostActivity(): Int {
        return posts.size
    }

    constructor() : this("")

}