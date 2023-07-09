package com.alexyach.kotlin.udemychat.ui.signin

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.databinding.FragmentSignInBinding
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.ui.userlist.UserListFragment
import com.alexyach.kotlin.udemychat.utils.LOG_TAG
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding: FragmentSignInBinding get() = _binding!!

    private val viewModel: SignInViewModel by lazy {
        ViewModelProvider(this)[SignInViewModel::class.java]
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var loginModeActive = false

    // [email, password, repeatPassword]
    private var validListTextFields =  mutableListOf(false, false, false)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d(LOG_TAG, "ApiException; $e")
            }
        }

        isLoginUser()
        validTextSign()
        clickListener()
    }

    private fun clickListener() {
        binding.signUpButton.setOnClickListener {
            signInUpMode()
            hideKeyboard(it)
        }

        binding.toggleLoginInUpTextView.setOnClickListener {
            toggleLoginMode()
        }

        binding.cvIconEmail.setOnClickListener {
            toggleGmailEmail(true)
        }

        binding.cvIconGmail.setOnClickListener {
            toggleGmailEmail(false)
        }

        binding.signUpGmailButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun isLoginUser() {
        if (auth.currentUser?.uid != null) {
            goToUserListFragment(auth.currentUser?.uid!!)
        }
    }

    private fun signInUpMode() {
        if (loginModeActive) {
            signInCurrentUserWithEmail()
        } else {
            signUpNewUserWithEmail()
        }
    }

    /** Email & Password */
    private fun signInCurrentUserWithEmail() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("myLog", "signInWithEmail:success")

                    val currentUser = auth.currentUser
                    goToUserListFragment(currentUser?.uid?:"")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("myLog", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireActivity(),
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }

    private fun signUpNewUserWithEmail() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val firebaseUser = auth.currentUser
                    createUser(firebaseUser)
                    goToUserListFragment(firebaseUser?.uid ?: "")

                    Log.d("myLogs", "createUserWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("myLogs", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireActivity(),
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }
    private fun createUser(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val user = UserModel(
                name = binding.userName.text.toString().trim(),
                email = firebaseUser.email ?: "",
                id = firebaseUser.uid
            )

            viewModel.saveUserToFirebase(user)
        } else {
            toast("User not found")
        }
    }

    /** Gmail */
    private fun signInWithGoogle() {
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(LOG_TAG, "Success SignIn")

                /** Записати, якщо в перший раз */
                val newUser: Boolean = task.result.additionalUserInfo?.isNewUser ?: true
                if (newUser) {
                    val user: FirebaseUser? = auth.currentUser
                    createUserGmail(user)
                    Log.d(LOG_TAG, "firebaseAuthWithGoogle: newUser: $newUser")
                }

                isLoginUser()
            } else {
                Log.d(LOG_TAG, "ERROR Sign In by Google")
            }
        }
    }

    private fun createUserGmail(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val user = UserModel(
                name = firebaseUser.displayName!!,
                email = firebaseUser.email!!,
                id = firebaseUser.uid
            )

            viewModel.saveUserToFirebase(user)
        }
    }
    /** End Gmail */

    private fun toggleLoginMode() {
        if (loginModeActive) {
            loginModeActive = false
            with(binding) {
                signUpButton.text = resources.getText(R.string.sign_up)
                toggleLoginInUpTextView.text = resources.getText(R.string.tap_sign_in)
                userName.visibility = View.VISIBLE
                repeatPassword.visibility = View.VISIBLE

                userName.text = null
                inputPassword.text = null
                inputRepeatPassword.text = null
                inputEmail.text = null
                validListTextFields = mutableListOf(false, false, false)
                binding.signUpButton.isEnabled = false

            }


        } else {
            loginModeActive = true
            with(binding) {
                signUpButton.text = resources.getText(R.string.sign_in)
                toggleLoginInUpTextView.text = resources.getText(R.string.tap_sign_up)
                userName.visibility = View.GONE
                repeatPassword.visibility = View.GONE

                inputPassword.text = null
                inputEmail.text = null
                validListTextFields = mutableListOf(false, false, false)
                binding.signUpButton.isEnabled = false
            }
        }
    }

    private fun toggleGmailEmail(isEmail: Boolean) {
        if (isEmail) {
            with(binding) {
                llSignByEmailAndPassword.visibility = View.VISIBLE
                llSignByGmail.visibility = View.GONE
                cvIconEmail.elevation = 8F
                cvIconGmail.elevation = 0F
                iconEmail.setTextColor(resources.getColor(R.color.purple_700, null))
                iconGmail.setTextColor(resources.getColor(R.color.gray_light, null))
            }
        } else {
                with(binding) {
                    llSignByEmailAndPassword.visibility = View.GONE
                    llSignByGmail.visibility = View.VISIBLE
                    cvIconEmail.elevation = 0F
                    cvIconGmail.elevation = 8F
                    iconEmail.setTextColor(resources.getColor(R.color.gray_light, null))
                    iconGmail.setTextColor(resources.getColor(R.color.purple_700, null))
                }
            }
    }

    private fun validTextSign() {
        val checkPassword: Pattern = Pattern.compile(".{6,}")

        // Проверяем email
        binding.inputEmail.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) return@setOnFocusChangeListener

            val text: String = (v as EditText).text.trim().toString()

            if (text.isEmpty() || !text.contains("@")) {
                binding.email.error = resources.getText(R.string.bad_email)
                binding.email.boxStrokeColor = Color.RED
                validListTextFields[0] = false
            } else {
                binding.email.error = null
                binding.email.boxStrokeColor = Color.GREEN
                validListTextFields[0] = true
            }
            if (isEnableSignButton()) binding.signUpButton.isEnabled = true
        }

        // Проверяем Password
        binding.inputPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) return@setOnFocusChangeListener

            val text: String = (v as EditText).text.toString()
                if (checkPassword.matcher(text).matches()) {
                    binding.password.error = null
                    binding.password.boxStrokeColor = Color.GREEN
                    validListTextFields[1] = true

                    // Якщо просто логінимся
                    if (loginModeActive) validListTextFields[2] = true
                } else {
                    binding.password.error = resources.getText(R.string.min_character)
                    binding.password.boxStrokeColor = Color.RED
                    validListTextFields[1] = false
                }
            if (isEnableSignButton()) binding.signUpButton.isEnabled =
                true
        }

        // Проверяем repeatPassword
        if (!loginModeActive) {
            binding.inputRepeatPassword.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) return@setOnFocusChangeListener

                val text: String = (v as EditText).text.trim().toString()

                if (text == binding.inputPassword.text?.trim().toString()) {
                    binding.repeatPassword.error = null
                    binding.repeatPassword.boxStrokeColor = Color.GREEN
                    validListTextFields[2] = true
//                    Log.d("myLogs", "validTextSign(): ${text} = ${binding.inputPassword.text} true")
                } else {
                    binding.repeatPassword.error = resources.getText(R.string.no_match_passwords)
                    binding.repeatPassword.boxStrokeColor = Color.RED
                    validListTextFields[2] = false
                }
                if (isEnableSignButton()) binding.signUpButton.isEnabled =
                    true
            }
        }

    }

    private fun isEnableSignButton(): Boolean {
        val valid = true
        for (validItem in validListTextFields) {
            if (!validItem) return false
        }
        return valid
    }

    private fun goToUserListFragment(userId: String) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, UserListFragment.newInstance(userId))
            .commit()
    }

    // Сховати клавіатуру
    private fun hideKeyboard(view: View?) {
        val imm = requireContext()
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance() = SignInFragment()
    }

}