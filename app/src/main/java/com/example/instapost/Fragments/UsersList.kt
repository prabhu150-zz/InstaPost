package com.example.instapost.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.instapost.Activities.FragmentManager
import com.example.instapost.Model.User
import com.example.instapost.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.search_users_posts.*
import kotlinx.android.synthetic.main.user_profile_row.view.*


class UsersList : Fragment() {


    private var DebugTag = "UsersListFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_users_posts, container, false)
    }

    override fun onStart() {
        super.onStart()
        retrieveUsers()
    }

    private fun logDebug(message: String) {
        Log.d(DebugTag, message)
    }

    private fun notifyToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun retrieveUsers() {

        val userDatabaseRef = FirebaseDatabase.getInstance().reference.child("/users/")

        val getAllUsersQuery = userDatabaseRef.orderByChild("postActivity")
        val adapter = GroupAdapter<ViewHolder>()
        var currentUser: User
        val listOfUsers = arrayListOf<UserProfile>()


        getAllUsersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                logDebug("Couldnt Retrieve Users! Reason: ${error.message}")
            }

            override fun onDataChange(data: DataSnapshot) {
                data.children.forEach {
                    currentUser = it.getValue(User::class.java) ?: User()
                    logDebug("Current User $currentUser")
                    if (currentUser.nickName.isNotEmpty()) {
                        listOfUsers.add(UserProfile(currentUser))
                    }
                }

                listOfUsers.reverse()

                for (currUser in listOfUsers)
                    adapter.add(currUser)

                adapter.setOnItemClickListener { item, view ->
                    view.visibility = View.GONE

                    val dashboardFragment = DashBoard()
                    val args = Bundle()
                    args.putString(getString(R.string.request), getString(R.string.custom_user_dashboard_request))
                    args.putString(
                        getString(R.string.userNicknameFragmentArg),
                        view.findViewById<TextView>(R.id.userName).text.toString()
                    )
                    dashboardFragment.arguments = args
                    (activity as FragmentManager).replaceFragment(dashboardFragment)
                }

                profileList.adapter = adapter

            }

        })


    }

}

class UserProfile(val currentUser: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_profile_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // called for each and every item

        viewHolder.itemView.userName.text = currentUser.nickName
        viewHolder.itemView.num_Posts.text = (currentUser.getPostActivity().toString() + " posts")
        Picasso.get().load(currentUser.profilePic).into(viewHolder.itemView.previewProfilePic)
    }
}
