package com.example.gymlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.model.WorkoutSessionEntity
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private val onDeleteClick: (WorkoutSessionEntity) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private var sessions = emptyList<WorkoutSessionEntity>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewSessionTitle)
        val timeTextView: TextView = itemView.findViewById(R.id.textViewSessionTime)

        fun bind(session: WorkoutSessionEntity) {
            // Ideally we'd show the routine name here, but for now we show start time
            titleTextView.text = "Session #${session.id}"
            timeTextView.text = dateFormat.format(Date(session.startTime))
            
            itemView.setOnLongClickListener {
                onDeleteClick(session)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_history, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun setData(newSessions: List<WorkoutSessionEntity>) {
        this.sessions = newSessions
        notifyDataSetChanged()
    }
    
    fun getSessionAt(position: Int): WorkoutSessionEntity = sessions[position]
}
