package com.example.a7minuteworkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.a7minuteworkout.databinding.WorkoutHistoryRowBinding

// delete listener add in parameters
//val deleteListener: (id : Int) -> Unit
class WorkoutHistoryAdapter(
    private val items: ArrayList<HistoryEntity>,val deleteListener: (id : Int) -> Unit
): RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder>(){

    class ViewHolder(binding: WorkoutHistoryRowBinding ) : RecyclerView.ViewHolder(binding.root){
        val llMain = binding.llMain
        val sno = binding.sNO
        val date = binding.date
        val time = binding.time
        val ivDelete = binding.ivDeleteRecord
        //delete image view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(WorkoutHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]
        holder.sno.text = item.id.toString()
        holder.date.text = item.date
        holder.time.text = item.time
        holder.ivDelete.setOnClickListener{
            deleteListener.invoke(item.id)
        }

        if (position % 2 == 0) {
            holder.llMain.setBackgroundColor(
                ContextCompat.getColor(context, R.color.lightGrey)
            )
        } else {
            holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }

    }
}