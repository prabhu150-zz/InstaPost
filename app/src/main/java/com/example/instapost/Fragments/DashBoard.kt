package com.example.instapost.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instapost.Model.Post
import com.example.instapost.Model.User
import com.example.instapost.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.dashboard_posts.*
import kotlinx.android.synthetic.main.dashboard_posts.view.*
import kotlinx.android.synthetic.main.post_card_template.view.*


class DashBoard : Fragment() {

    private val HASH_TAG_IMAGE =
        "https://firebasestorage.googleapis.com/v0/b/instapost-bb5c4.appspot.com/o/profile-pics%2Fhash_tag.png?alt=media&token=63f4ddb8-08e2-4163-937b-fa6161e7aac9"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dashboard_posts, null)
    }

    override fun onStart() {
        super.onStart()

        var currentUser = User()
        val request = arguments?.getString(getString(R.string.request), "")

        var nickName = ""
        var currentHashTag = ""

        myPosts.layoutManager = LinearLayoutManager(activity)


        when (request) {
            getString(R.string.personal_dashboard_request) -> getCurrentUser()
            getString(R.string.custom_hashtag_dashboard_request) -> {
                // posts by hashtags
                currentHashTag = arguments?.getString(getString(R.string.hashTagFragmentArg), "") ?: ""
                retrievePosts(currentHashTag)
            }
            getString(R.string.custom_user_dashboard_request) -> {
                // posts by user
                nickName = arguments?.getString(getString(R.string.userNicknameFragmentArg), "") ?: ""
                logDebug("For custom user got $nickName")
                currentUser = getUserByNickName(nickName)
            }
            else -> getCurrentUser()
        }

    }


    fun logDebug(message: String) {
        Log.d("Dashboard", message)
    }

    fun toastNotification(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }


    private fun retrievePosts(currentUser: User) {

        val postRef =
            FirebaseDatabase.getInstance().reference.child("/users/${currentUser.userID}/posts/").orderByChild("postID")

        val postList = arrayListOf<PostProfile>()

        val adapter = GroupAdapter<ViewHolder>()

        postRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                logDebug("Couldnt Retrieve Posts! Reason: ${error.message}")
            }

            override fun onDataChange(data: DataSnapshot) {

                var currentPost: Post
                logDebug("Trying to get posts for user ${currentUser.nickName}")

                data.children.forEach {
                    currentPost = it.getValue(Post::class.java) ?: Post(
                        "",
                        "",
                        "",
                        ""
                    )
                    logDebug("Current Post ${currentPost.caption}")

                    if (currentPost.caption.isNotEmpty()) {
                        postList.add(PostProfile(currentPost, currentUser))
                    }

                }
                postList.reverse() // gets them in most recent order

                for (post in postList)
                    adapter.add(post)

                myPosts.adapter = adapter

            }

        })


    }


    private fun retrievePosts(currentHashTag: String) {
        logDebug("Current HashTag #$currentHashTag")
        val hashRef = FirebaseDatabase.getInstance().reference.child("/hashtags/$currentHashTag/posts/")

        val adapter = GroupAdapter<ViewHolder>()

        hashRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                logDebug("Couldnt Retrieve Posts! Reason: ${error.message}")
            }

            override fun onDataChange(data: DataSnapshot) {

                var currentPost = Post("", "", "", "")
                val numPosts = data.children.count()
                logDebug("Num Posts $numPosts")
                toastNotification("Showing all posts with #$currentHashTag")
                data.children.forEach {
                    currentPost = it.getValue(Post::class.java) ?: Post(
                        "",
                        "",
                        "",
                        ""
                    )
                    logDebug("Current Post ${currentPost.caption}")

                    if (currentPost.caption.isNotEmpty()) {
                        adapter.add(PostProfile(currentPost, User()))
                    }

                }


                myPosts.adapter = adapter

                constraintLayout.userNumPosts.text = ("$numPosts posts")
                Picasso.get().load(HASH_TAG_IMAGE).into(constraintLayout.imageView)
                constraintLayout.userName.text = currentHashTag
                constraintLayout.userNickName.text = ""
            }
        })


    }

    private fun getCurrentUser(): User {

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        var currentUser = User()

        if (userEmail.isEmpty())
            throw IllegalStateException("Invalid user!")


        logDebug("User email : ${userEmail}")

        val query3 = FirebaseDatabase.getInstance().getReference("/users/")
            .orderByChild("email")
            .equalTo(userEmail)

        val postListener = object : ValueEventListener {
            override fun onDataChange(tasks: DataSnapshot) {
                if (tasks.exists())
                    for (currentTask in tasks.children) {
                        currentUser = currentTask.getValue(User::class.java) ?: User()
                        logDebug("Inside on data change module")
                        logDebug("Got current user nickname ${currentUser.nickName}")
                    }

                logDebug("User nickname post retrieval : ${currentUser.nickName}")

                updateUI(currentUser)
                retrievePosts(currentUser)

                logDebug("User id : ${currentUser.userID}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDebug("""Database error:${databaseError.message}""")
            }
        }

        query3.addListenerForSingleValueEvent(postListener)


        return currentUser


    }


    private fun getUserByNickName(nickName: String): User {

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        var currentUser = User()

        if (userEmail.isEmpty())
            throw IllegalStateException("Invalid user!")

        val query3 = FirebaseDatabase.getInstance().getReference("/users/")
            .orderByChild("nickName")
            .equalTo(nickName)

        val postListener = object : ValueEventListener {
            override fun onDataChange(tasks: DataSnapshot) {
                logDebug("Inside on data change module")
                if (tasks.exists())
                    for (currentTask in tasks.children) {
                        currentUser = currentTask.getValue(User::class.java) ?: User()
                        toastNotification("Viewing all posts by ${currentUser.nickName}")
                    }

                logDebug("User nickname : ${currentUser.userID}")
                retrievePosts(currentUser)
                updateUI(currentUser)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDebug("""Database error:${databaseError.message}""")
            }
        }

        query3.addListenerForSingleValueEvent(postListener)

        logDebug("For custom users, recieved ${currentUser.name} as name")

        return currentUser
    }

    private fun updateUI(currentUser: User) {
        Picasso.get().load(currentUser.profilePic).into(constraintLayout.imageView)
        constraintLayout.userName.text = currentUser.name
        constraintLayout.userNickName.text = currentUser.nickName
        constraintLayout.userNumPosts.text = ("${currentUser.getPostActivity()} posts")
    }

}

class PostProfile(val currentPost: Post, val currentUser: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.post_card_template
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.postCaption.text = currentPost.caption
        viewHolder.itemView.postedBy.text = (currentUser.nickName)
        Picasso.get().load(currentPost.imgURL).into(viewHolder.itemView.postPic)

    }
}
