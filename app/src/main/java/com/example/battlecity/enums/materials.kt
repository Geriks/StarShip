package com.example.battlecity.enums

import com.example.battlecity.R


const val CELLS_SIMPLE_ELEMENT = 1
const val CELLS_FORT_WIDTH = 4
const val CELLS_FORT_HEIGHT = 3
const val  CELLS_SHIP_SIZE = 3



enum class Material(
        val shipCanGoThrough: Boolean,
        val bulletCanGoThrough: Boolean,
        val simpleBulletCanDestroy: Boolean,
        val elementsAmountOnScreen: Int,
        val width: Int,
        val height: Int,
        val image: Int?
) {
    EMPTY(
            shipCanGoThrough = true,
            bulletCanGoThrough = true,
            simpleBulletCanDestroy = true,
            elementsAmountOnScreen = 0,
            width = 0,
            height = 0,
            image = null
    ),
    OSTERS(
            shipCanGoThrough = false,
            bulletCanGoThrough = false,
            simpleBulletCanDestroy = true,
            elementsAmountOnScreen = 0,
            width = CELLS_SIMPLE_ELEMENT,
            height = CELLS_SIMPLE_ELEMENT,
            image = R.drawable.osters
    ),
    CONCRETE(
            shipCanGoThrough = false,
            bulletCanGoThrough = false,
            simpleBulletCanDestroy = false,
            elementsAmountOnScreen = 0,
            width = CELLS_SIMPLE_ELEMENT,
            height = CELLS_SIMPLE_ELEMENT,
            image = R.drawable.concrete
    ),
    TUMAN(
            shipCanGoThrough = true,
            bulletCanGoThrough = true,
            simpleBulletCanDestroy = false,
            elementsAmountOnScreen = 0,
            width = CELLS_SIMPLE_ELEMENT,
            height = CELLS_SIMPLE_ELEMENT,
            image = R.drawable.tuman
    ),
    FORT(
            shipCanGoThrough = false,
            bulletCanGoThrough = false,
            simpleBulletCanDestroy = true,
            elementsAmountOnScreen = 1,
            width = CELLS_FORT_WIDTH,
            height = CELLS_FORT_HEIGHT,
            image = R.drawable.fort
    ),
    ENEMY_SHIP(
            shipCanGoThrough = false,
            bulletCanGoThrough = false,
            simpleBulletCanDestroy = true,
            elementsAmountOnScreen = 0,
            width = CELLS_SHIP_SIZE,
            height = CELLS_SHIP_SIZE,
            image = R.drawable.enemy
    ),
    PLAYER_SHIP(
            shipCanGoThrough = false,
            bulletCanGoThrough = false,
            simpleBulletCanDestroy = true,
            elementsAmountOnScreen = 1,
            width = CELLS_SHIP_SIZE,
            height = CELLS_SHIP_SIZE,
            image = R.drawable.ship
    ),
}