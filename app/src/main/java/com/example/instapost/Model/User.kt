package com.example.instapost.Model

class User(
    var userID: String,
    var name: String,
    var email: String,
    var password: String,
    var nickName: String,
    var profilePic: String = "https://firebasestorage.googleapis.com/v0/b/instapost-bb5c4.appspot.com/o/profile-pics%2Favatar-single-360.png?alt=media&token=137ad0f3-aca7-4096-aa95-eeed0a630890"
) {


    var userBio: String = ""
    var posts = mutableListOf<Post>()

    fun addPost(currentPost: Post) {
        posts.add(currentPost)
    }

    fun getPostActivity(): Int {
        return posts.size
    }

    constructor() : this("", "", "", "", "", "")

}