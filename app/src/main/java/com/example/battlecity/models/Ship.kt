package com.example.battlecity.models

import android.view.View
import android.widget.FrameLayout
import com.example.battlecity.activities.CELL_SIZE
import com.example.battlecity.drawers.EnemyDrawer
import com.example.battlecity.enums.Direction
import com.example.battlecity.enums.Material
import com.example.battlecity.utils.*
import kotlin.random.Random


class Ship constructor(
        val element: Element,
        var direction: Direction,
        private val enemyDrawer: EnemyDrawer
) {
    fun move(
            direction: Direction,
            container: FrameLayout,
            elementsOnContainer: List<Element>
    ) {
        val view = container.findViewById<View>(element.viewId) ?: return
        val currentCoordinate = view.getViewCoordinate()
        this.direction = direction
        view.rotation = direction.rotation
        val nextCoordinate = getShipNextCoordinate(view)
        if (view.checkViewCanMoveThroughBorder(nextCoordinate)
                && element.checkShipCanMoveThroughMaterial(nextCoordinate, elementsOnContainer)
        ) {
            emulateViewMoving(container, view)
            element.coordinate = nextCoordinate
            generateRandomDirectionForEnemyShip()
        } else {
            element.coordinate = currentCoordinate
            (view.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordinate.top
            (view.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordinate.left
            changeDirectionForEnemyShip()
        }
    }

    private fun generateRandomDirectionForEnemyShip() {
        if (element.material != Material.ENEMY_SHIP) {
            return
        }
        if (checkIfChanceBiggerThanRandom(10)) {
            changeDirectionForEnemyShip()
        }
    }

    private fun changeDirectionForEnemyShip() {
        if (element.material == Material.ENEMY_SHIP) {
            val randomDirection = Direction.values()[Random.nextInt(Direction.values().size)]
            this.direction = randomDirection
        }
    }

    private fun emulateViewMoving(container: FrameLayout, view: View) {
        container.runOnUiThread {
            container.removeView(view)
            container.addView(view, 0)
        }
    }




    private fun getShipNextCoordinate(view: View): Coordinate {
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        when (direction) {
            Direction.UP -> {
                (view.layoutParams as FrameLayout.LayoutParams).topMargin += -CELL_SIZE
            }
            Direction.DOWN -> {
                (view.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }
            Direction.RIGHT -> {
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
            Direction.LEFT -> {
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin += -CELL_SIZE
            }
        }
        return Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
    }

    private fun Element.checkShipCanMoveThroughMaterial(
            coordinate: Coordinate,
            elementsOnContainer: List<Element>
    ): Boolean {
        for (anyCoordinate in getShipCoordinates(coordinate)) {
            var element = getElementByCoordinates(anyCoordinate, elementsOnContainer)
            if (element == null) {
                element = getShipByCoordinates(anyCoordinate, enemyDrawer.ships)
            }
            if (element != null && !element.material.shipCanGoThrough) {
                if (this == element) {
                    continue
                }
                return false
            }
        }
        return true
    }

    private fun getShipCoordinates(topLeftCoordinate: Coordinate): List<Coordinate> {
        val coordinateList = mutableListOf<Coordinate>()
        coordinateList.add(topLeftCoordinate)
        coordinateList.add(Coordinate(topLeftCoordinate.top + CELL_SIZE, topLeftCoordinate.left)) //bottom_left
        coordinateList.add(Coordinate(topLeftCoordinate.top, topLeftCoordinate.left + CELL_SIZE)) //top_right
        coordinateList.add(
                Coordinate(
                        topLeftCoordinate.top + CELL_SIZE,
                        topLeftCoordinate.left + CELL_SIZE
                )
        )
        return coordinateList
    }
}
