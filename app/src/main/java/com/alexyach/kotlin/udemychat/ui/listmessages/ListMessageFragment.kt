package com.alexyach.kotlin.udemychat.ui.listmessages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.databinding.FragmentListMessageBinding
import com.alexyach.kotlin.udemychat.domain.MessageModel
import com.alexyach.kotlin.udemychat.ui.signin.SignInFragment
import com.alexyach.kotlin.udemychat.utils.KEY_CURRENT_USER_ID
import com.alexyach.kotlin.udemychat.utils.LOG_TAG
import com.google.firebase.database.DatabaseError
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import okhttp3.internal.notifyAll

//const val KEY_CURRENT_USER_ID = "currentUser"
//const val IMAGE_FROM_LOCAL = 111

class ListMessageFragment : Fragment() {

    private var _binding: FragmentListMessageBinding? = null
    private val binding: FragmentListMessageBinding get() = _binding!!

    private val viewModel: ListMessageViewModel by lazy {
        ViewModelProvider(this)[ListMessageViewModel::class.java]
    }

    private lateinit var adapter: ListMessageAdapter
    private var listMessage = listOf<MessageModel>()

    private lateinit var currentUserId: String
    private var userName = "Username"

    // UpLoad File From Local
    private val getContext = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            upLoadImage(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListMessageBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentUserId = arguments?.run {
            getString(KEY_CURRENT_USER_ID)
        } ?: ""
        viewModel.currentUserId = currentUserId

//        setUpAdapter(listMessage)
        setClickButtons()
        changeMessageEditText()
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.messageListUiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is MessageListUiState.Success -> showMessagesList(uiState.list)
                MessageListUiState.Loading -> showLoading()
                is MessageListUiState.Error -> showError(uiState.error)
            }
        }

        viewModel.userNameLiveData.observe(viewLifecycleOwner) {
            userName = it
//            Log.d(LOG_TAG, "observe name= $it")
        }
    }

    private fun showMessagesList(dataList: List<MessageModel>) {
        binding.progressBarMessageList.visibility = View.GONE
        listMessage = dataList

//        adapter.notifyDataSetChanged()
        setUpAdapter(dataList)
    }

    private fun showLoading() {
        binding.progressBarMessageList.visibility = View.VISIBLE
    }

    private fun showError(error: DatabaseError) {
        binding.progressBarMessageList.visibility = View.GONE
        setUpAdapter(
            listOf(
                MessageModel(
                    name = "ERROR",
                    message = error.message
                )
            )
        )
    }

    private fun setUpAdapter(messageList: List<MessageModel>) {
        adapter = ListMessageAdapter(messageList)
        binding.messagesListRecycler.adapter = adapter

        adapter.notifyItemInserted(adapter.itemCount - 1)
//        adapter.notifyDataSetChanged()

        binding.messagesListRecycler.smoothScrollToPosition(adapter.itemCount - 1)
    }

    private fun setClickButtons() {
        binding.messageSendButton.setOnClickListener {
            sendMessage()
            binding.messageSendEditText.text = null
            hideKeyboard(it)
        }

        binding.photoSendButton.setOnClickListener {
            getContext.launch("image/*")
        }
    }

    private fun upLoadImage(uri: Uri) {
        viewModel.upLoadImageFromFirebase(uri, userName)
    }

    private fun sendMessage() {
        viewModel.sendMessageToFirebase(
            MessageModel(
                name = userName,
                message = binding.messageSendEditText.text.toString()
            )
        )
    }

    private fun changeMessageEditText() {
        // Обмеження кількості символів
        binding.messageSendEditText.filters = arrayOf(InputFilter.LengthFilter(500))

        binding.messageSendEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Активує кнопку Send
                binding.messageSendButton.isEnabled = s.toString().trim().isNotEmpty()
            }

            // Не використовуємо
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    // Сховати клавіатуру
    private fun hideKeyboard(view: View?) {
        val imm = requireContext()
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

    }

    /** Menu */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sign_out, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                viewModel.signOut()
                goToSignInFragment()
            }
        }
        return true
    }

    private fun goToSignInFragment() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SignInFragment.newInstance())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance(userId: String): ListMessageFragment {
            return ListMessageFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CURRENT_USER_ID, userId)
                }
            }
        }
    }

}