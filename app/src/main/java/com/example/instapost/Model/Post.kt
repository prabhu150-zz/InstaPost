package com.example.instapost.Model

class Post(
    var postID: String,
    var caption: String,
    var timestamp: String,
    var imgURL: String
) {
    var list = arrayListOf<HashTag>()

    constructor() : this("", "", "", "")

}