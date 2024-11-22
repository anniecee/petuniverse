package com.example.pet_universe.ui.livePets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pet_universe.R

data class Pet(
    val name: String,
    val price: Int,
    val type: String,
    val description: String,
    val imageUrl: String,
    val petLocation: String
)

class PetAdapter(
    private var petList: List<Pet>,
    private val onItemClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petImageView: ImageView = itemView.findViewById(R.id.petImageView)
        val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        val petPriceTextView: TextView = itemView.findViewById(R.id.petPriceTextView)
        val petTypeTextView: TextView = itemView.findViewById(R.id.petTypeTextView)
        val petDescriptionTextView: TextView = itemView.findViewById(R.id.petDescriptionTextView)
//        val petLocationTextView: TextView = itemView.findViewById(R.id.locationTextView)

        init {
            itemView.setOnClickListener {
                onItemClick(petList[adapterPosition]) // Trigger the click listener with the clicked pet
            }
        }

        fun bindData(listing: Pet) {
            if (listing.imageUrl != null) {
                val imageUrl = listing.imageUrl
                petImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.pet_accessories_logo)
                    error(R.drawable.pet_accessories_logo)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pet_in_list, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = petList[position]
        holder.bindData(pet)
        holder.petNameTextView.text = pet.name
        holder.petPriceTextView.text = "$${pet.price}" // Format price
        holder.petTypeTextView.text = pet.type

        val maxLength = 80
        val cleanedDescription = pet.description.replace("\n", " ") // Replace newlines with spaces
        val truncatedDescription = if (cleanedDescription.length > maxLength) {
            cleanedDescription.take(maxLength) + "..."
        } else {
            cleanedDescription
        }
        holder.petDescriptionTextView.text = truncatedDescription

//        holder.petLocationTextView.text = pet.petLocation
    }

    override fun getItemCount(): Int = petList.size

    // Update the pet list and notify the adapter of data changes
    fun updatePets(newPetList: List<Pet>) {
        petList = newPetList
        notifyDataSetChanged()
    }
}
