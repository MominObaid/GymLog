package com.example.gymlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.model.UserProfile
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

class ProfilePillAdapter(
    private val onProfileClick: (UserProfile) -> Unit,
    private val onLongClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<ProfilePillAdapter.ViewHolder>() {

    private var profiles = emptyList<UserProfile>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardProfilePill)
        val nameText: TextView = itemView.findViewById(R.id.tvPillName)
        val avatar: ShapeableImageView = itemView.findViewById(R.id.ivPillAvatar)

        fun bind(profile: UserProfile) {
            nameText.text = profile.name
            avatar.setStrokeColor(android.content.res.ColorStateList.valueOf(profile.avatarColor))
            avatar.setBackgroundColor(profile.avatarColor)
            avatar.alpha = 0.8f
            
            val context = itemView.context
            if (profile.isActive) {
                card.strokeWidth = 6
                card.strokeColor = ContextCompat.getColor(context, R.color.workout_blue)
                card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.workout_blue_light))
                nameText.setTextColor(ContextCompat.getColor(context, R.color.workout_blue))
            } else {
                card.strokeWidth = 2
                card.strokeColor = ContextCompat.getColor(context, R.color.card_stroke_color)
                card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_light))
                nameText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_light))
            }

            itemView.setOnClickListener { onProfileClick(profile) }
            itemView.setOnLongClickListener { 
                onLongClick(profile)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_pill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size

    fun setData(newProfiles: List<UserProfile>) {
        this.profiles = newProfiles
        notifyDataSetChanged()
    }
}
