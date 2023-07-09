package com.alexyach.kotlin.udemychat.ui.listmessages

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexyach.kotlin.udemychat.domain.MessageModel
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.utils.FIREBASE_IMAGES
import com.alexyach.kotlin.udemychat.utils.FIREBASE_MESSAGE
import com.alexyach.kotlin.udemychat.utils.FIREBASE_USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

/** UI State */
sealed interface MessageListUiState {
    data class Success(val list: List<MessageModel>) : MessageListUiState
    data class Error(val error: DatabaseError) : MessageListUiState
    object Loading : MessageListUiState
}

/** *** */

class ListMessageViewModel : ViewModel() {
    private val database: FirebaseDatabase = Firebase.database
    private val messagesReference: DatabaseReference = database.getReference(FIREBASE_MESSAGE)
    private val usersReference: DatabaseReference = database.getReference(FIREBASE_USERS)
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference.child(FIREBASE_IMAGES)


    private val _messageListUiState: MutableLiveData<MessageListUiState> = MutableLiveData()
    val messageListUiState: LiveData<MessageListUiState> = _messageListUiState

    private val _userNameLiveData: MutableLiveData<String> = MutableLiveData()
    val userNameLiveData: LiveData<String> = _userNameLiveData

    var currentUserId = ""
    var recipientUserId = ""
    var recipientName = MutableLiveData<String>()


    init {
        _messageListUiState.value = MessageListUiState.Loading
        messageFirebaseListener()
        usersFirebaseListener()
    }

    fun sendMessageToFirebase(message: MessageModel) {
        messagesReference.push().setValue(message)
    }

    fun signOut() {
        Firebase.auth.signOut()
    }

    private fun messageFirebaseListener() {
        messagesReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val listMessage = mutableListOf<MessageModel>()

                for (s in snapshot.children) {
                    val message = s.getValue(MessageModel::class.java)

//                    Log.d(LOG_TAG, " before if() message= ${message?.name}")
//                    Log.d(LOG_TAG, "MessageListener currentUserId= $currentUserId")
//                    Log.d(LOG_TAG, "MessageListener recipientUserId= $recipientUserId")

                    // Моє повідомлення
                    if (message != null &&
                        message.senderId == currentUserId &&
                        message.recipientId == recipientUserId
                    ) {

                        message.isMine = true
                        listMessage.add(message)

                        // Плвідомлення оппонента
                    } else if (message != null &&
                        message.senderId == recipientUserId &&
                        message.recipientId == currentUserId
                    ) {
                        message.isMine = false
                        listMessage.add(message)
                        recipientName.value = message.name
//                        Log.d(LOG_TAG, "VieModel recipientName = ${message.name}")
                    }

                }
                _messageListUiState.value = MessageListUiState.Success(listMessage)

            }

            override fun onCancelled(error: DatabaseError) {
                _messageListUiState.value = MessageListUiState.Error(error)
            }

        })
    }

    private fun usersFirebaseListener() {
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (s in snapshot.children) {
                    val user = s.getValue(UserModel::class.java)

                    if (user?.id == currentUserId) {
//                if (user?.id == FirebaseAuth.getInstance().currentUser?.uid) {
                        _userNameLiveData.value = user.name
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _userNameLiveData.value = "No Name"
            }
        })
    }

    /** Upload Image */
    fun upLoadImageFromFirebase(uri: Uri, userName: String) {
        val imageReference = uri.lastPathSegment?.let { storageReference.child(it) }
        if (imageReference != null) {
            val uploadTask: UploadTask = imageReference.putFile(uri)

            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Шлях до зображення в Firebase
                    val downloadUri = task.result
                    // Відправляємо message
                    sendMessageToFirebase(
                        MessageModel(
                            name = userName,
                            imageUrl = downloadUri.toString(),
                            senderId = currentUserId,
                            recipientId = recipientUserId
                        )
                    )
                } else {
                    // Handle failures
                    // ...
                }
            }
        }
    }

}

