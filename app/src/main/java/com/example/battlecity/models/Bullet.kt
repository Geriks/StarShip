package com.example.battlecity.models

import android.view.View
import com.example.battlecity.enums.Direction

class Bullet(
        val view: View,
        val direction: Direction,
        val ship: Ship,
        var canMoveFurther: Boolean = true
) {

}