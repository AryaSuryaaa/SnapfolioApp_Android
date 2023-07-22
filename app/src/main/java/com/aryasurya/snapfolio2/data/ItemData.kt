package com.aryasurya.snapfolio2.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ItemData(var id: String,
               var title: String = "",
               var description: String = "",
               var image: String = "") : Parcelable