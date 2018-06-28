package org.paykey.gliphrecorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import java.io.File

class MainActivity : AppCompatActivity() {

    private val wordList: ArrayList<Word> = ArrayList()
    private val gson = Gson()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
            return
        }

        val testName = findViewById<EditText>(R.id.testName)
        val writingPad = findViewById<WritingPad>(R.id.writingPad)
        writingPad.setOnTouchListener { v, event ->
            testName.clearFocus()
            false
        }
        val container = findViewById<FlexboxLayout>(R.id.container)

        val nextButton = findViewById<Button>(R.id.next).apply {
            setOnClickListener {
                val strokeList = writingPad.strokeList.toList()
                wordList.add(Word(strokeList))
                writingPad.clear()
                if (strokeList.isEmpty())
                    return@setOnClickListener

                val pathView = PathView(context)
                pathView.init(strokeList.flatMap { stroke -> stroke.actions })
                val layoutParams = FlexboxLayout.LayoutParams((writingPad.width * 0.1).toInt(),
                        (writingPad.height * 0.1).toInt())
                container.addView(pathView, layoutParams)
            }
        }

        findViewById<Button>(R.id.done).apply {
            setOnClickListener {
                val name = testName.text?.toString()
                if (name.isNullOrEmpty()) {
                    Toast.makeText(context, "Enter test name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val paykeyDir = File(Environment.getExternalStorageDirectory(), "paykey")
                if (!paykeyDir.exists()) {
                    paykeyDir.mkdir()
                }
                val testFile = File(paykeyDir, "${name?.replace(" ", "_")}.json")

                if (testFile.exists()) {
                    Toast.makeText(context, "Experiment with this name already exist", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                nextButton.performClick()
                container.removeAllViews()
                testName.setText("")

                val message = gson.toJson(wordList)
                testFile.writeText(message)
                println(message)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        recreate()
    }
}


data class Word(val strokes: List<Stroke>)
