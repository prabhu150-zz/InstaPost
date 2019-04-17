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
import com.example.instapost.Model.HashTag
import com.example.instapost.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.hash_tag_rows.view.*
import kotlinx.android.synthetic.main.search_hashtags_posts.*


class HashTagsList : Fragment() {

    private val FETCH_POSTS_BY_HASHTAGS = 22
    private var DebugTag = "UsersListFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_hashtags_posts, container, false)
    }

    override fun onStart() {
        super.onStart()

        retrieveHashTags()

    }

    private fun logDebug(message: String) {
        Log.d(DebugTag, message)
    }

    private fun notifyToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun retrieveHashTags() {

        val hashtagRef = FirebaseDatabase.getInstance().reference.child("/hashtags/")


        val getAllHashTags = hashtagRef.orderByChild("postActivity")
        val adapter = GroupAdapter<ViewHolder>()
        val listHashTags = arrayListOf<HashTagProfile>()


        getAllHashTags.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                logDebug("Couldnt Retrieve Hashtags! Reason: ${error.message}")
            }

            override fun onDataChange(data: DataSnapshot) {
                data.children.forEach {
                    val currentHashTag = it.getValue(HashTag::class.java)
                    logDebug("Current User $currentHashTag")

                    if (currentHashTag != null) {
                        listHashTags.add(HashTagProfile(currentHashTag))
                    }
                }


                listHashTags.reverse()

                for (hashTag in listHashTags)
                    adapter.add(hashTag)


                adapter.setOnItemClickListener { item, view ->
                    view.visibility = View.GONE

                    val dashboardFragment = DashBoard()
                    val args = Bundle()
                    args.putString(getString(R.string.request), getString(R.string.custom_hashtag_dashboard_request))
                    args.putString(
                        getString(R.string.hashTagFragmentArg),
                        view.findViewById<TextView>(R.id.hashtag_name).text.toString().substring(1)
                    )
                    dashboardFragment.arguments = args
                    (activity as FragmentManager).replaceFragment(dashboardFragment)
                }


                hashTagList.adapter = adapter

            }

        })


    }

}

class HashTagProfile(private val hashTag: HashTag) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.hash_tag_rows
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // called for each and every item

        viewHolder.itemView.hashtag_name.text = ("#" + hashTag.hashTagText)
        viewHolder.itemView.num_Posts.text = (hashTag.getPostActivity().toString() + " posts")

    }
}
