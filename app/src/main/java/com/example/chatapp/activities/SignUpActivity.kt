package com.example.chatapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.databinding.ActivitySignUpBinding
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivitySignUpBinding
private lateinit var preferenceManager: PreferenceManager
private lateinit var encodedImage: String
@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners()
    {
        binding.textSignIn.setOnClickListener { onBackPressed() }
        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails())
            {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
    private fun signUp()
    {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = hashMapOf<String, Any>(
            Constants.KEY_NAME to binding.inputName.text.toString(),
            Constants.KEY_EMAIL to binding.inputEmail.text.toString(),
            Constants.KEY_PASSWORD to binding.inputPassword.text.toString(),
            Constants.KEY_IMAGE to encodedImage
        )
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }.addOnFailureListener { exception ->
                loading(false)
                exception.message?.let { showToast(it) }
            }
    }


    private fun encodeImage(bitmap: Bitmap): String
    {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)

        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)

    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                try
                {
                    applicationContext.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE
                        encodedImage = encodeImage(bitmap)
                    }
                }
                catch (e: FileNotFoundException)
                {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isValidSignUpDetails(): Boolean
    {
        if(encodedImage.isEmpty())
        {
            showToast("Select profile image")
            return false
        }
        else if (binding.inputName.text.toString().trim().isEmpty())
        {
            showToast("Enter name")
            return false
        }
        else if (binding.inputEmail.text.toString().trim().isEmpty())
        {
            showToast("Enter email")
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches())
        {
            showToast("Enter valid email")
            return false
        }
        else if (binding.inputPassword.text.toString().trim().isEmpty())
        {
            showToast("Enter password")
            return false
        }
        else if (binding.inputConfirmPassword.text.toString().trim().isEmpty())
        {
            showToast("Confirm your password")
            return false
        }
        else if (binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString())
        {
            showToast("Password must be the same in both fields")
            return false
        }
        else
        {
            return true
        }
    }

    private fun loading (isLoading: Boolean)
    {
        if (isLoading)
        {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
        else
        {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }
}


