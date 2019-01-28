package com.pdg.colourselector.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ServerResponse(
    var data: Colour?,
    var id: Int
//    var data: Int?,
//    var id: Int
) : Parcelable
