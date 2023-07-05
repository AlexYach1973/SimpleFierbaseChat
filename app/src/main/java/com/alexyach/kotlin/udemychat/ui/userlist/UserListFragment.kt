package com.alexyach.kotlin.udemychat.ui.userlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.databinding.FragmentUserListBinding
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.ui.listmessages.ListMessageFragment
import com.alexyach.kotlin.udemychat.ui.signin.SignInFragment
import com.alexyach.kotlin.udemychat.utils.KEY_CURRENT_USER_ID

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding: FragmentUserListBinding get() = _binding!!

    private val viewModel: UserListViewModel by lazy {
        ViewModelProvider(this)[UserListViewModel::class.java]
    }

    private lateinit var adapter: UserListAdapter

    //    private var userList = listOf<UserModel>()
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = arguments?.run {
            getString(KEY_CURRENT_USER_ID)
        } ?: ""
        viewModel.currentUserId = currentUserId

        observeLiveData()
        setupToolbar()

    }

    private fun observeLiveData() {
        viewModel.userListUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserListUiState.Success -> showUsersList(state.dataList)
                is UserListUiState.Error -> showError(state.error.message)
                UserListUiState.Loading -> showLoading()
            }


        }
    }

    private fun showUsersList(dataList: List<UserModel>) {
        binding.progressBarUserList.visibility = View.GONE
        setAdapter(dataList)
        //            adapter.notifyDataSetChanged()
    }

    private fun showError(error: String) {
        binding.progressBarUserList.visibility = View.GONE
        setAdapter(
            listOf(
                UserModel(
                    name = error
                )
            )
        )
    }

    private fun showLoading() {
        binding.progressBarUserList.visibility = View.VISIBLE
    }

    private fun setAdapter(dataList: List<UserModel>) {
        adapter = UserListAdapter(dataList, object : OnUserListItemClickListener {
            override fun onUserListItemClick(position: Int) {
                goToChat(dataList[position].id)
//                Toast.makeText(requireContext(), "User id: ${dataList[position].id}", Toast.LENGTH_SHORT).show()
            }
        })
        binding.userListRecyclerView.adapter = adapter
        binding.userListRecyclerView
            .addItemDecoration(DividerItemDecoration(
                requireActivity(),
            DividerItemDecoration.VERTICAL))
    }

    private fun goToChat(recipientId: String) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                ListMessageFragment.newInstance(
                    viewModel.currentUserId,
                    recipientId
                )
            )
            .addToBackStack(null)
            .commit()
    }

    /** ToolBar */
    private fun setupToolbar() {
        with(binding.toolbarUserList) {
            title = "users List"
            inflateMenu(com.alexyach.kotlin.udemychat.R.menu.menu_sign_out)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    com.alexyach.kotlin.udemychat.R.id.menu_sign_out -> {
                        viewModel.signOut()
                        goToSignInFragment()
                        true
                    }

                    else -> false
                }
            }
        }
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
        fun newInstance(userId: String): UserListFragment {
            return UserListFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CURRENT_USER_ID, userId)
                }
            }
        }
    }

}