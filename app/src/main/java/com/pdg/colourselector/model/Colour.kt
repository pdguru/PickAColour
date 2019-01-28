package com.pdg.colourselector.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Colour(
var name: String,
var resID: Int
): Parcelable