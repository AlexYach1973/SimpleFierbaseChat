package com.alexyach.kotlin.udemychat.ui.signin

import androidx.lifecycle.ViewModel
import com.alexyach.kotlin.udemychat.domain.MessageModel
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.utils.FIREBASE_USERS
import com.alexyach.kotlin.udemychat.utils.NO_LOGIN_USER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignInViewModel : ViewModel() {

    private var database: FirebaseDatabase = Firebase.database
    private var usersReference: DatabaseReference = database.getReference(FIREBASE_USERS)

    private val auth : FirebaseAuth = Firebase.auth

    fun saveUserToFirebase(user: UserModel) {
        usersReference.push().setValue(user)
    }


}