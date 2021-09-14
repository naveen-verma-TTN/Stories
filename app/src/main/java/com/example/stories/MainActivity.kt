package com.example.stories

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.stories.databinding.ActivityMainBinding
import com.example.stories.storiesprogressview.StoriesProgressView
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager


class MainActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    companion object {
        private const val PROGRESS_COUNT = 6
    }

    private val durations = longArrayOf(
        5000L, 2500L, 3500L, 4000L, 5000L, 5000
    )

    private var counter = 0

    private val resources = intArrayOf(
        R.drawable.sample1,
        R.drawable.sample2,
        R.drawable.sample3,
        R.drawable.sample4,
        R.drawable.sample5,
        R.drawable.sample6
    )

    var pressTime = 0L
    var limit = 500L

    private lateinit var binding: ActivityMainBinding

    private val onTouchListener: View.OnTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressTime = System.currentTimeMillis()
                    binding.stories.pause()
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    binding.stories.resume()
                    return limit < now - pressTime
                }
            }
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.addFlags(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stories.setStoriesCount(PROGRESS_COUNT) // <- set stories
//        binding.stories.setStoryDuration(1200L) // <- set a story duration

        binding.stories.setStoriesCountWithDurations(durations)

        binding.stories.setStoriesListener(this) // <- set listener


        startShow()


        bindListeners()
    }

    private fun startShow() {
        binding.image.setImageResource(resources[0])
        binding.stories.startStories() // <- start progress
    }

    private fun bindListeners() {
        // bind reverse view
        binding.reverse.setOnClickListener { binding.stories.reverse() }
        binding.reverse.setOnTouchListener(onTouchListener)

        // bind skip view
        binding.skip.setOnClickListener { binding.stories.skip() }
        binding.skip.setOnTouchListener(onTouchListener)
    }

    override fun onNext() {
        binding.image.setImageResource(resources[++counter])
    }

    override fun onPrev() {
        binding.image.setImageResource(resources[--counter])
    }

    override fun onComplete() {
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroy() {
        binding.stories.destroy()
        super.onDestroy()
    }
}