package com.medtime.findrjob.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.medtime.findrjob.SeekerAccountDetails

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Start SeekerAccountDetails Activity directly
        val intent = Intent(requireContext(), SeekerAccountDetails::class.java)
        startActivity(intent)

        // Optionally, finish the current activity (if you're transitioning out of the current one)
        requireActivity().finish()

        // Return an empty view since we are directly transitioning to the new activity
        return View(requireContext()) // Return a dummy view
    }
}
