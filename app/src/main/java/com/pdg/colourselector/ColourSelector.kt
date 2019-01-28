package com.pdg.colourselector

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.gson.JsonObject
import com.pdg.colourselector.apicalls.ApiHelper
import com.pdg.colourselector.model.PayloadForRequest
import com.pdg.colourselector.model.Colour
import com.pdg.colourselector.model.ServerResponse
import com.pdg.colourselector.utils.CustomListAdapter
import com.pdg.colourselector.utils.SharedPref
import kotlinx.android.synthetic.main.activity_colour.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder



class ColourSelector : AppCompatActivity() {

    lateinit var token: String
    lateinit var apiHelper: ApiHelper

    val coloursArray = arrayOf(
        Colour("Red", R.color.red), Colour("Lime", R.color.lime), Colour("Blue", R.color.blue),
        Colour("Yellow", R.color.yellow), Colour("Cyan", R.color.cyan), Colour("Magenta", R.color.magenta),
        Colour("Maroon", R.color.maroon), Colour("Green", R.color.green), Colour("Purple", R.color.purple),
        Colour("Navy", R.color.navy)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colour)

        token = SharedPref.getToken(this@ColourSelector)
        Log.i(Constants.TAG, "🎨 Colour Token: $token")

        getSavedColour()

        colourList.adapter = CustomListAdapter(this, coloursArray)

        colourList.setOnItemClickListener { parent, view, position, id ->
            setSelectedColour(coloursArray[position].resID)
            Log.i(Constants.TAG, "🎨 resID: ${coloursArray[position].resID}")
            saveSelectedColour(coloursArray[position])
        }
    }

    private fun setSelectedColour(clr: Int) {
        when (clr) {
            -1 -> {
                selectedColour.background = resources.getDrawable(android.R.color.white, null)
                selectedColour.text = "No colour selected"
                selectedColour.setTextColor(Color.BLACK)
                selectedColour.setShadowLayer(2f, 0f, 0f, Color.WHITE)
            }
            else -> {
                selectedColour.background = resources.getDrawable(clr, null)
                selectedColour.text = "Selected colour"
                selectedColour.setTextColor(Color.WHITE)
                selectedColour.setShadowLayer(2f, 0f, 0f, Color.BLACK)
            }
        }
    }

    private fun getSavedColour() {

        initRetrofitBuilder()
        val call = apiHelper.getColour(
            "api/v1/storage/${SharedPref.getStorageID(this@ColourSelector)}",
            token, Constants.CONTENT_TYPE
        )
        Log.i(
            Constants.TAG, "➡️ saved call: " + call.request()
        )

        call.enqueue(object : Callback<ServerResponse> {

            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful) {
                    var clr = response.body()?.data?.resID
                    if (clr != null) {
                        Log.i(
                            Constants.TAG,
                            "👍 onResponse: ${response.body()?.data?.name} ${response.body()?.data?.resID}"
//                            "👍 onResponse: ${response.body()?.data}"
                        )
                        setSelectedColour(clr)
                    } else {
                        Log.i(Constants.TAG, "ℹ️ onResponse: No colour saved in cloud")
                        Toast.makeText(
                            this@ColourSelector,
                            "ℹ️ No colour stored on the server.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Log.d(Constants.TAG, "❌ GET -> onResponse: SOMETHING WENT WRONG: " + response.toString())
                    logErrorCode(response.code())
                    Toast.makeText(
                        this@ColourSelector,
                        "⚠️ Colour could not be retrieved. Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.i(Constants.TAG, "❌ GET -> onResponse: FAILED:" + t.message)
                Toast.makeText(
                    this@ColourSelector,
                    "⚠️ Colour could not be retrieved. Try again later.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun saveSelectedColour(colour: Colour) {

        val json = JsonObject()
        json.addProperty("name", colour.name)
        json.addProperty("resID", colour.resID)
        Log.i(Constants.TAG, "➡️ saving colour: " + json.toString())

        initRetrofitBuilder()
        val call = apiHelper.saveColour(
            "api/v1/storage/${SharedPref.getStorageID(this@ColourSelector)}",
            token, Constants.CONTENT_TYPE,
            PayloadForRequest(json)
        )
        Log.i(Constants.TAG, "➡️ saving call: " + call.request())

        call.enqueue(object : Callback<ServerResponse> {

            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ColourSelector,
                        "✅️ Colour saved successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d(Constants.TAG, "❌ Save -> onResponse: SOMETHING WENT WRONG: " + response.toString())
                    logErrorCode(response.code())
                    Toast.makeText(
                        this@ColourSelector,
                        "⚠️ Colour could not be saved. Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.i(Constants.TAG, "❌ Save -> onResponse: FAILED:" + t.message)
                Toast.makeText(this@ColourSelector, "⚠️ Colour could not be saved. Try again later.", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun logErrorCode(responseCode: Int) {
        when {
            (responseCode > 500) -> {
                Log.d(Constants.TAG, "❌ FAIL -> CODE: ${responseCode} Server issue ❌")
            }
            (responseCode == 400) -> {
                Log.d(Constants.TAG, "❌ FAIL -> CODE: ${responseCode} Bad/malformed request ❌")
            }
            (responseCode == 401) -> {
                Log.d(Constants.TAG, "❌ FAIL -> CODE: ${responseCode} Unauthorised ❌")
            }
            (responseCode == 403) -> {
                Log.d(Constants.TAG, "❌ FAIL -> CODE: ${responseCode} Forbidden ❌")
            }
            else -> {
                Log.d(Constants.TAG, "❌ FAIL -> CODE: ${responseCode} Unknown issue ❌")
            }
        }
    }

    fun initRetrofitBuilder() {

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val builder = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
        val retrofit = builder.build()
        apiHelper = retrofit.create(ApiHelper::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_delete -> {
                deleteAndForget()
            }
            R.id.action_retry -> {
                getSavedColour()
            }
            else -> {
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAndForget() {
        initRetrofitBuilder()

        val call = apiHelper.delete(
            "api/v1/storage/${SharedPref.getStorageID(this@ColourSelector)}",
            token, Constants.CONTENT_TYPE
        )
        Log.i(Constants.TAG, "➡️ delete call: " + call.request())

        call.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    Toast.makeText(
                        this@ColourSelector,
                        "✅️ Details successfully deleted.",
                        Toast.LENGTH_LONG
                    ).show()
                    SharedPref.setToken("",this@ColourSelector)
                    SharedPref.setStorageID(-1,this@ColourSelector)
                    finish()
                } else {
                    Log.d(Constants.TAG, "❌ Delete -> onResponse: SOMETHING WENT WRONG: " + response.toString())
                    logErrorCode(response.code())
                    Toast.makeText(
                        this@ColourSelector,
                        "⚠️ Data could not be deleted. Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.i(Constants.TAG, "❌ Delete -> onResponse: FAILED:" + t.message)
                Toast.makeText(this@ColourSelector, "⚠️ Could not delete. Try again later.", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }
}

