package com.pdg.colourselector

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.pdg.colourselector.apicalls.ApiHelper
import com.pdg.colourselector.model.AuthToken
import com.pdg.colourselector.model.ServerResponse
import com.pdg.colourselector.model.UserLoginCreds
import com.pdg.colourselector.utils.SharedPref
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    // UI references.
    private var mUnameView: EditText? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null

    lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        Log.i(Constants.TAG, "ℹ️ onCreate Login")
        mUnameView = findViewById<View>(R.id.uname) as EditText

        mPasswordView = findViewById<View>(R.id.password) as EditText
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)
    }

    override fun onStart() {
        super.onStart()

        Log.i(Constants.TAG, "ℹ️ onStart Login")
        Log.i(Constants.TAG, "🎨 Login️ Token: ${SharedPref.getToken(this@LoginActivity)}")

        if (!TextUtils.isEmpty(SharedPref.getToken(this@LoginActivity))) {
            goToNextActivity()
        } else {
            sign_in_button.setOnClickListener { attemptLogin() }
        }
    }

    private fun attemptLogin() {

        // Reset errors.
        mUnameView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val uname = mUnameView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = getString(R.string.error_incorrect_password)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid uname address.
        if (TextUtils.isEmpty(uname)) {
            mUnameView!!.error = getString(R.string.error_field_required)
            focusView = mUnameView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            //Actual login

            val loginTask = UserLoginCreds(uname, password)
            initRetrofitBuilder()
            val call = apiHelper.login(loginTask)
            Log.i(Constants.TAG, "➡️ Login call: " + call.request())

            call.enqueue(object : Callback<AuthToken> {

                override fun onResponse(call: Call<AuthToken>, response: Response<AuthToken>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        Log.i(Constants.TAG, "👍 Token: $token")
                        if (token != null) {
                            SharedPref.setToken(token, this@LoginActivity)
                            setupCloudStorage()
                        }
                    } else {
                        Log.d(Constants.TAG, "❌ onResponse: SOMETHING WENT WRONG: " + response.toString())
                        showProgress(false)
                        Toast.makeText(this@LoginActivity, "Something went wrong. Try again later.", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                override fun onFailure(call: Call<AuthToken>, t: Throwable) {
                    Log.i(Constants.TAG, "❌ onResponse: FAILED:" + t.message)
                }
            })
        }
    }

    private fun setupCloudStorage() {
        initRetrofitBuilder()
        val call = apiHelper.createStorage(SharedPref.getToken(this@LoginActivity), Constants.CONTENT_TYPE)
        Log.i(Constants.TAG, "➡️ create storage call: " + call.request())

        call.enqueue(object : Callback<ServerResponse> {

            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful) {
                    val id = response.body()?.id
                    Log.i(Constants.TAG, "👍 ID returned: $id")
                    if (id != null) {
                        SharedPref.setStorageID(id, this@LoginActivity)
                        goToNextActivity()
                    }
                } else {
                    Log.d(Constants.TAG, "❌ CREATE -> onResponse: SOMETHING WENT WRONG: " + response.toString())
                    Toast.makeText(
                        this@LoginActivity,
                        "⚠️ Server could not be reached. Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.i(Constants.TAG, "❌ CREATE -> onResponse: FAILED:" + t.message)
                Toast.makeText(this@LoginActivity, "⚠️ Colour could not be saved. Try again later.", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun goToNextActivity() {
        val intent = Intent(this@LoginActivity, ColourSelector::class.java)
        startActivity(intent)
    }


    fun initRetrofitBuilder() {
        val builder = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL + Constants.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val retrofit = builder.build()
        apiHelper = retrofit.create(ApiHelper::class.java)
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}