package com.example.a7minuteworkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minuteworkout.databinding.ActivityHistoryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity() {
    private var binding: ActivityHistoryBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarHistoryActivity)
        supportActionBar?.title = "History"

        if(supportActionBar != null)
        {
            supportActionBar?.title = "History"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarHistoryActivity?.setNavigationOnClickListener{
            onBackPressed()
            finish()
            val intent = Intent(this@HistoryActivity,MainActivity::class.java)
            startActivity(intent)
        }

//        val historyDao = (application as HistoryApp).db.historyDao()
        lifecycleScope.launch {
            val historyDatabase = HistoryDatabase.getInstance(this@HistoryActivity)
            val historyDao = historyDatabase.historyDao()
            historyDao.fetchHistory().collect{
                val list = ArrayList(it)
                setRecyclerView(list,historyDao)
            }
        }

    }

    private fun setRecyclerView(historyList: ArrayList<HistoryEntity>,historyDao : HistoryDao){
        if (historyList.isNotEmpty()){
            val historyAdapter =  WorkoutHistoryAdapter(historyList) { deleteID ->
                deleteRecord(
                    deleteID,
                    historyDao
                )
            }

            binding?.rgHistory?.layoutManager =LinearLayoutManager(this)
            binding?.rgHistory?.adapter = historyAdapter
            binding?.rgHistory?.visibility = View.VISIBLE
            binding?.noRecord?.visibility = View.GONE
        }
        else
        {
            binding?.rgHistory?.visibility = View.GONE
            binding?.noRecord?.visibility = View.VISIBLE

        }
    }

    private  fun deleteRecord(id: Int, historyDao: HistoryDao){
        val builder =AlertDialog.Builder(this)
        builder.setTitle("Delete Record")

        builder.setMessage("Are you sure you want to delete this record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes"){dialogInterface,_->

            lifecycleScope.launch {
                historyDao.delete(HistoryEntity(id))
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No") { dialogInterface,_ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}