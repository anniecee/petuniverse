package com.example.pet_universe.ui.livePets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing

data class Pet(val name: String, val price: Double, val description: String, val imageResId: Int)

class PetAdapter(
    private var petList: List<Pet>,
    private val onItemClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petImageView: ImageView = itemView.findViewById(R.id.petImageView)
        val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        val petPriceTextView: TextView = itemView.findViewById(R.id.petPriceTextView)
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
        holder.petPriceTextView.text = pet.price.toString()
        holder.petDescriptionTextView.text = pet.description
    }

    override fun getItemCount(): Int = petList.size

    // Update the pet list and notify the adapter of data changes
    fun updatePets(newPetList: List<Pet>) {
        petList = newPetList
        notifyDataSetChanged()
    }
}
