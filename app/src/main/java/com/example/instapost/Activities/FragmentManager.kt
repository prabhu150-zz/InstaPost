package com.example.instapost.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.instapost.Fragments.CreatePost
import com.example.instapost.Fragments.DashBoard
import com.example.instapost.Fragments.HashTagsList
import com.example.instapost.Fragments.UsersList
import com.example.instapost.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.container.*


class FragmentManager : AppCompatActivity() {

    private val dashboardFragment: Fragment = DashBoard()
    private val hashtagsFragment: Fragment = HashTagsList()
    private val postsFragment: Fragment = CreatePost()
    private val usersFragment: Fragment = UsersList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container)

        if (FirebaseAuth.getInstance().currentUser == null)
            redirectToLogin()

        replaceFragment(postsFragment)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.logout, menu)
        return true
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        FirebaseAuth.getInstance().signOut()
        redirectToLogin()
        return true
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.dashboard -> {
                val args = Bundle()
                args.putString(
                    getString(R.string.request),
                    getString(R.string.personal_dashboard_request)
                ) // personal db
                dashboardFragment.arguments = args
                replaceFragment(dashboardFragment)
                actionBar?.title = getString(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.searchUsers -> {
                replaceFragment(usersFragment)
                actionBar?.title = getString(R.string.searchUsersTitle)
                return@OnNavigationItemSelectedListener true
            }
            R.id.searchByHashtag -> {
                replaceFragment(hashtagsFragment)
                actionBar?.title = getString(R.string.searchByHashtagTitle)
                return@OnNavigationItemSelectedListener true
            }
            R.id.createNewPost -> {
                replaceFragment(postsFragment)
                actionBar?.title = getString(R.string.createNewPostTitle)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}

