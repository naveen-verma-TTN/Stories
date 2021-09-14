package com.example.stories

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.stories.databinding.ActivityMainBinding
import com.example.stories.storiesprogressview.StoriesProgressView
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer


class MainActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    private var player: SimpleExoPlayer? = null

    companion object {
        private const val PROGRESS_COUNT = 6
        private const val TAG = "MainActivity"
    }

    private val durations = longArrayOf(
        7000L, 2500L, 5500L, 6500L, 5000L, 7500L
    )

    private var link1 =
        "https://livesim.dashif.org/livesim/chunkdur_1/ato_7/testpic4_8s/Manifest.mpd"
    private var link2 =
        "https://dash.akamaized.net/dash264/TestCasesIOP33/adapatationSetSwitching/5/manifest.mpd"

    private var counter = 0

    private val resources: ArrayList<Any> = ArrayList()

    var pressTime = 0L
    var limit = 500L
    private var touchFlag = false

    private lateinit var binding: ActivityMainBinding

    private val onTouchListener: View.OnTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressTime = System.currentTimeMillis()
                    binding.stories.pause()
                    player?.pause()
                    touchFlag = true
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    binding.stories.resume()
                    player?.play()
                    touchFlag = false
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

        resources.apply {
            add(link2)
            add(R.drawable.sample3)
            add(link1)
            add(link2)
            add(R.drawable.sample6)
            add(link1)
        }

        binding.stories.setStoriesCount(PROGRESS_COUNT) // <- set stories
//        binding.stories.setStoryDuration(1200L) // <- set a story duration

        binding.stories.setStoriesCountWithDurations(durations)

        initializePlayer()

        binding.stories.setStoriesListener(this) // <- set listener

        startShow()

        bindListeners()
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
            }
    }


    private fun playbackStateListener() = object : Player.EventListener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> {

                    binding.progessView.visibility = View.VISIBLE

//                    binding.stories.pause()

                    "ExoPlayer.STATE_BUFFERING -"
                }
                ExoPlayer.STATE_READY -> {

                    binding.progessView.visibility = View.GONE

//                    binding.stories.resume()

                    if(!touchFlag)
                        player?.play()

                    "ExoPlayer.STATE_READY     -"
                }
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.e(TAG, "changed state to $stateString")
        }
    }

    private fun startShow() {
        if (resources.isNotEmpty() && resources[0] is Int) {
            binding.image.setImageResource(resources[0] as Int)

            toggleView(true)

        } else {
            // player code

            val mediaItem = MediaItem.fromUri(resources[0] as String)
            player?.setMediaItem(mediaItem)

            player?.prepare()

            player?.addListener(playbackStateListener())

            toggleView(false)
        }

        binding.stories.startStories() // <- start progress

        if (resources.isNotEmpty() && resources[0] is String) {
//            obtainOnTouch()
        }
    }

    private fun obtainOnTouch() {
        binding.skip.dispatchTouchEvent(
            MotionEvent.obtain(
                0,
                0,
                MotionEvent.ACTION_DOWN,
                100f,
                100f,
                0.5f,
                5f,
                0,
                1f,
                1f,
                0,
                0
            )
        );
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
        resources[++counter]

        if (resources[counter] is Int) {
            player?.stop()

            binding.image.setImageResource(resources[counter] as Int)

            toggleView(true)
        } else {
            // player code

            val mediaItem = MediaItem.fromUri(resources[counter] as String)
            player?.setMediaItem(mediaItem)
            player?.prepare()

            toggleView(false)
        }
    }

    override fun onPrev() {
        resources[--counter]

        if (resources[counter] is Int) {
            player?.stop()

            binding.image.setImageResource(resources[counter] as Int)

            toggleView(true)
        } else {
            // player code

            val mediaItem = MediaItem.fromUri(resources[counter] as String)
            player?.setMediaItem(mediaItem)
            player?.prepare()

            toggleView(false)
        }
    }

    override fun onComplete() {
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show()

        player?.release()
    }

    private fun toggleView(flag: Boolean) {
        if (flag) {
            binding.image.visibility = View.VISIBLE
            binding.videoView.visibility = View.GONE
        } else {
            binding.videoView.visibility = View.VISIBLE
            binding.image.visibility = View.GONE
        }
    }


    override fun onDestroy() {
        binding.stories.destroy()
        super.onDestroy()
    }
}