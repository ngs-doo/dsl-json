package com.dslplatform.androidkotlin

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.dslplatform.androidkotlin.databinding.ActivityMainBinding
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val dslJson = DSL.JSON()

        val concrete = Concrete()
        concrete.x = 11
        concrete.y = 23
        val parent = ParentClass()
        parent.a = 5
        parent.b = 6

        val instance = Model(
            string = "Hello World!",
            number = 42,
            integers = listOf(1, 2, 3),
            decimals = HashSet(listOf(BigDecimal.ONE, BigDecimal.ZERO)),
            uuids = arrayOf(UUID(1L, 2L), UUID(3L, 4L)),
            longs = Vector(listOf(1L, 2L)),
            nested = listOf(Nested(), null),
            inheritance = parent,
            iface = WithCustomCtor(5, 6),
            person = Person("first name", "last name", 35),
            states = Arrays.asList(State.HI, State.LOW),
            jsonObject = JsonObjectReference(43, "abcd"),
            jsonObjects = Collections.singletonList(JsonObjectReference(34, "dcba")),
            time = LocalTime.of(12, 15),
            times = listOf(null, LocalTime.of(8, 16)),
            abs = concrete,
            absList = listOf<Abstract?>(concrete, null, concrete),
            decimal2 = BigDecimal.TEN,
            intList = ArrayList(listOf(123, 456)),
            map = mapOf("abc" to 678, "array" to arrayOf(2, 4, 8))
        )

        val tv = findViewById<TextView>(R.id.tvHello)
        try {
            val os = ByteArrayOutputStream()
            //serialize into stream
            dslJson.serialize(instance, os)

            val stream = ByteArrayInputStream(os.toByteArray())
            //deserialized using stream API
            val result = dslJson.deserialize(Model::class.java, stream)
            tv.setText(result.string)
        } catch (ex: IOException) {
            tv.setText(ex.message)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}