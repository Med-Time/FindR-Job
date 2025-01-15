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
    ): View? {
        val intent = Intent(requireContext(), SeekerAccountDetails::class.java)
        startActivity(intent)
        requireActivity().supportFragmentManager.popBackStack()

        // Return an empty view since we're immediately transitioning
        return View(requireContext())
    }
}
