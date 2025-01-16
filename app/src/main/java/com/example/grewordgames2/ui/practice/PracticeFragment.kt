package com.example.grewordgames2.ui.practice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grewordgames2.R
import com.example.grewordgames2.practicePage
import com.example.grewordgames2.databinding.FragmentPracticeBinding

class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var difficulty = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(PracticeViewModel::class.java)

        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val difficulty0 = binding.difficulty0
        val difficulty1 = binding.difficulty1
        val difficulty2 = binding.difficulty2
        val difficulty3 = binding.difficulty3
        val difficulty4 = binding.difficulty4

        difficulty0.setOnCheckedChangeListener { buttonView, isChecked ->
            difficulty = 0
        }
        difficulty1.setOnCheckedChangeListener { buttonView, isChecked ->
            difficulty = 1
        }
        difficulty2.setOnCheckedChangeListener { buttonView, isChecked ->
            difficulty = 2
        }
        difficulty3.setOnCheckedChangeListener { buttonView, isChecked ->
            difficulty = 3
        }
        difficulty4.setOnCheckedChangeListener { buttonView, isChecked ->
            difficulty = 4
        }

        val submitDifficultyButton = binding.submitDifficulty
        submitDifficultyButton.setOnClickListener(){
            if (difficulty == -1){
                Toast.makeText(activity, "Please select a difficulty", Toast.LENGTH_SHORT).show()
            }
            else if(difficulty == 0) {
                val accessPracticeIntent = Intent(activity, practicePage::class.java).apply{
                    putExtra("difficulty", difficulty.toString())
                }
                startActivity(accessPracticeIntent)
            }
            else {
                Toast.makeText(activity, "Not implemented yet", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}