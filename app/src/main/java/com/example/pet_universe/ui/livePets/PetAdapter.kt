package com.example.pet_universe.ui.livePets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.R

data class Pet(val name: String, val gender: String, val description: String, val imageResId: Int)

class PetAdapter(
    private val petList: List<Pet>,
    private val onItemClick: (Pet) -> Unit // Lambda function for item click listener
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petImageView: ImageView = itemView.findViewById(R.id.petImageView)
        val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        val petGenderTextView: TextView = itemView.findViewById(R.id.petGenderTextView)
        val petDescriptionTextView: TextView = itemView.findViewById(R.id.petDescriptionTextView)

        init {
            itemView.setOnClickListener {
                onItemClick(petList[adapterPosition]) // Trigger the click listener with the clicked pet
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet_in_list, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = petList[position]
        holder.petImageView.setImageResource(pet.imageResId)
        holder.petNameTextView.text = pet.name
        holder.petGenderTextView.text = pet.gender
        holder.petDescriptionTextView.text = pet.description
    }

    override fun getItemCount(): Int = petList.size
}