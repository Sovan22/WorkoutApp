package com.example.a7minuteworkout

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minuteworkout.databinding.ActivityExerciseBinding
import com.example.a7minuteworkout.databinding.DialogCustomBackConfirmationBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var binding : ActivityExerciseBinding? = null
    private var restTimer: CountDownTimer? = null
    private var restProgress  = 0
    private val constraintSet = ConstraintSet()
    private var restTimerDuration : Long = 1

    private var exTimer: CountDownTimer? = null
    private var exProgress  = 0
    private var exerciseTimerDuration : Long = 1
    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition  = -1

    private var tts: TextToSpeech? = null
    private var speechText : String? = null

    private var player: MediaPlayer? = null

    private var exerciseAdapter : ExerciseStatusAdapter? = null
    private var historyDatabase : HistoryDatabase? = null
    private var historyDao : HistoryDao? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        tts = TextToSpeech(this,this)
        exerciseList = Constants.defaultExerciseList()

        setSupportActionBar(binding?.toolbarExercise)
        if(supportActionBar != null)
        {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarExercise?.setNavigationOnClickListener {
            customDialogForBack()
        }
        setupRestView()
        setupExerciseStatusRecyclerView()


    }

    private fun customDialogForBack()
    {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.yesButton.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.noButton.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun setupExerciseStatusRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager =
            LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter

    }

    private fun setupRestView()
    {

        try{
            val soundURI = Uri.parse(
                "android.resource://com.example.a7minuteworkout/" + R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()

        }catch (e: Exception){
            e.printStackTrace()
        }

        restProgress = 0
        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        constraintSet.clone(binding?.layout)
        constraintSet.connect(
            binding?.tvTitle?.id ?: 0,          // Start view id
            ConstraintSet.BOTTOM,                // Start side
            binding?.flRestView?.id ?: 0,    // End view id
            ConstraintSet.TOP,                   // End side
            0                                    // Margin
        )
        constraintSet.applyTo(binding?.layout)
        binding?.tvExercise?.visibility = View.INVISIBLE
        binding?.exProgressView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE

        if(restTimer != null)
        {
            restTimer?.cancel()
            restProgress = 0
        }
        setRestProgressBar()
    }

    private fun setupExerciseView()
    {
        exProgress = 0
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE

        constraintSet.clone(binding?.layout)
        constraintSet.connect(
            binding?.tvTitle?.id ?: 0,          // Start view id
            ConstraintSet.BOTTOM,                // Start side
            binding?.exProgressView?.id ?: 0,    // End view id
            ConstraintSet.TOP,                   // End side
            0                                    // Margin
        )
        constraintSet.applyTo(binding?.layout)

        binding?.tvExercise?.visibility = View.VISIBLE
        binding?.exProgressView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE

        if(exTimer != null)
        {
            exTimer?.cancel()
            exProgress = 0
        }

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())

        binding?.tvExercise?.text = exerciseList!![currentExercisePosition].getName()

        speechText = binding?.tvExercise?.text.toString()
        speakOut(speechText!!)

        setExerciseProgressBar()

    }

    @SuppressLint("SetTextI18n")
    private fun setRestProgressBar(){
        binding?.progressBar?.progress = restProgress

        binding?.tvTitle?.text = "Get Ready for " + exerciseList!![currentExercisePosition+1].getName()
        speechText = binding?.tvTitle?.text.toString()
        speakOut(speechText!!)
        restTimer = object : CountDownTimer(restTimerDuration* 1000,1000){

            override fun onTick(p0 : Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()

            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()
                setupExerciseView()

            }

        }.start()
    }

    private fun setExerciseProgressBar()
    {
        binding?.exProgressBar?.progress = exProgress
        restTimer = object : CountDownTimer(exerciseTimerDuration*1000,1000){

            override fun onTick(p0 : Long) {
                exProgress++
                binding?.exProgressBar?.progress = 30 - exProgress
                binding?.exTimer?.text = (30 - exProgress).toString()

            }
            override fun onFinish() {

                exerciseList!![currentExercisePosition].setIsSelected(false)
                exerciseList!![currentExercisePosition].setIsCompleted(true)

                exerciseAdapter!!.notifyDataSetChanged()

                Toast.makeText(this@ExerciseActivity, "Exercise ended",Toast.LENGTH_SHORT).show()
                if(currentExercisePosition< exerciseList?.size!! - 1)
                        setupRestView()
                else
                {
                    finish()
                    lifecycleScope.launch {
                        historyDao = (application as HistoryApp).db.historyDao()
                        addRecord(historyDao!!)

                    }
                    val intent = Intent(this@ExerciseActivity,onFinish::class.java)
                    startActivity(intent)
                }


            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }

        if(restTimer != null)
        {
            restTimer?.cancel()
            restProgress = 0
        }

        if(exTimer != null)
        {
            exTimer?.cancel()
            exProgress = 0
        }

        if(player !=null){
            player!!.stop()
        }
        binding = null
    }

    override fun onInit(status: Int) {
        if( status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "Language not supported!")
            }
        }
        else{
            Log.e("TTS", "Initialization Failed!")
        }
    }


    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    fun addRecord(historyDao: HistoryDao)
    {
        var sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.format(Date())
        sdf = SimpleDateFormat("HH:mm")
        val time = sdf.format(Date())

        lifecycleScope.launch{
            historyDao.insert(HistoryEntity(date = date, time = time))
        }
    }

}