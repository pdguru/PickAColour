package com.pdg.colourselector.viewmodels

import android.arch.lifecycle.ViewModel
import com.pdg.colourselector.R
import com.pdg.colourselector.model.Colour

class ColourSelectorViewModel : ViewModel() {
    val coloursArray = arrayOf(
        Colour("Red", R.color.red), Colour("Lime", R.color.lime), Colour("Blue", R.color.blue),
        Colour("Yellow", R.color.yellow), Colour("Cyan", R.color.cyan), Colour("Magenta", R.color.magenta),
        Colour("Maroon", R.color.maroon), Colour("Green", R.color.green), Colour("Purple", R.color.purple),
        Colour("Navy", R.color.navy)
    )
}
