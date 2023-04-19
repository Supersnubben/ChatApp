package com.example.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.chatapp.adapters.UsersAdapter
import com.example.chatapp.databinding.ActivityUsersBinding
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { finish() }
    }

    private fun getUsers()
    {
        loading(true)
        val database = FirebaseFirestore.getInstance()

        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if(task.isSuccessful && task.result != null) {
                    val users = ArrayList<User>()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result) {
                        if (currentUserId.equals(queryDocumentSnapshot.id)) {
                            continue
                        }
                        val user: User = User()
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME) ?: ""
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL) ?: ""
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE) ?: ""
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN) ?: ""
                        users.add(user)
                    }
                    if (users.size > 0) {
                        val usersAdapter: UsersAdapter = UsersAdapter(users)
                        binding.usersRecyclerView.adapter = usersAdapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                }
                else {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        }
        else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}