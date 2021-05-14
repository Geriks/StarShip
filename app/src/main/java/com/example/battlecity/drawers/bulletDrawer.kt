package com.example.battlecity.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.battlecity.activities.CELL_SIZE
import com.example.battlecity.GameCore

import com.example.battlecity.enums.Direction
import com.example.battlecity.R
import com.example.battlecity.enums.Material
import com.example.battlecity.models.Bullet

import com.example.battlecity.models.Coordinate
import com.example.battlecity.models.Element
import com.example.battlecity.models.Ship
import com.example.battlecity.sounds.MainSoundPlayer
import com.example.battlecity.utils.*

private const val BULLET_WIDTH = 15
private const val BULLET_HEIGHT = 25

class BulletDrawer(
        private val container: FrameLayout,
        private val elements: MutableList<Element>,
        private val enemyDrawer: EnemyDrawer,
        private val soundManager: MainSoundPlayer,
        private val gameCore: GameCore
) {

    init {
        moveAllBullets()
    }

    private val allBullets = mutableListOf<Bullet>()

    fun addNewBulletForShip(ship: Ship) {
        val view = container.findViewById<View>(ship.element.viewId) ?: return
        if (ship.alreadyHasBullet()) return
        allBullets.add(Bullet(createBullet(view, ship.direction), ship.direction, ship))
        soundManager.bulletShot()
    }

    private fun Ship.alreadyHasBullet(): Boolean =
            allBullets.firstOrNull { it.ship == this } != null

    private fun moveAllBullets() {
        Thread(Runnable {
            while (true) {
                if (!gameCore.isPlaying()) {
                    continue
                }
                interactWithAllBullets()
                Thread.sleep(30)
            }
        }).start()
    }

    private fun interactWithAllBullets() {
        allBullets.toList().forEach { bullet ->
            val view = bullet.view
            if (bullet.canBulletGoFurther()) {
                when (bullet.direction) {
                    Direction.UP -> (view.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                    Direction.DOWN -> (view.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                    Direction.LEFT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                    Direction.RIGHT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin += BULLET_HEIGHT
                }
                chooseBehaviorInTermsOfDirections(bullet)
                container.runOnUiThread {
                    container.removeView(view)
                    container.addView(view)
                }
            } else {
                stopBullet(bullet)
            }
            bullet.stopIntersectingBullets()
        }
        removeInconsistentBullets()
    }

    private fun removeInconsistentBullets() {
        val removingList = allBullets.filter { !it.canMoveFurther }
        removingList.forEach {
            container.runOnUiThread {
                container.removeView(it.view)
            }
        }
        allBullets.removeAll(removingList)
    }

    private fun Bullet.stopIntersectingBullets() {
        val bulletCoordinate = this.view.getViewCoordinate()
        for (bulletInList in allBullets) {
            val coordinateInList = bulletInList.view.getViewCoordinate()
            if (this == bulletInList) {
                continue
            }
            if (coordinateInList == bulletCoordinate) {
                stopBullet(this)
                stopBullet(bulletInList)
                return
            }
        }
    }

    private fun Bullet.canBulletGoFurther() =
            this.view.checkViewCanMoveThroughBorder(this.view.getViewCoordinate()) && this.canMoveFurther

    private fun chooseBehaviorInTermsOfDirections(bullet: Bullet) {
        when (bullet.direction) {
            Direction.DOWN, Direction.UP -> {
                compareCollections(getCoordinatesForTopOrBottomDirection(bullet), bullet)
            }
            Direction.LEFT, Direction.RIGHT -> {
                compareCollections(getCoordinatesForLeftOrRightDirection(bullet), bullet)
            }
        }
    }

    private fun compareCollections(detectedCoordinatesList: List<Coordinate>, bullet: Bullet) {
        for (coordinate in detectedCoordinatesList) {
            var element = getShipByCoordinates(coordinate, enemyDrawer.ships)
            if (element == null) {
                element = getElementByCoordinates(coordinate, elements)
            }
            if (element == bullet.ship.element) {
                continue
            }
            removeElementsAndStopBullet(element, bullet)
        }
    }

    private fun removeElementsAndStopBullet(element: Element?, bullet: Bullet) {
        if (element != null) {
            if (bullet.ship.element.material == Material.ENEMY_SHIP && element.material == Material.ENEMY_SHIP) {
                stopBullet(bullet)
                return
            }
            if (element.material.bulletCanGoThrough) {
                return
            }
            if (element.material.simpleBulletCanDestroy) {
                stopBullet(bullet)
                removeView(element)
                removeElement(element)
                stopGameIfNecessary(element)
                removeShip(element)
            } else {
                stopBullet(bullet)
            }
        }
    }

    private fun removeElement(element: Element) {
        elements.remove(element)
    }

    private fun stopGameIfNecessary(element: Element) {
        if (element.material == Material.PLAYER_SHIP || element.material == Material.FORT) {
            gameCore.destroyPlayerOrBase(enemyDrawer.getPlayerScore())
        }
    }

    private fun removeShip(element: Element) {
        val shipsElements = enemyDrawer.ships.map { it.element }
        val shipIndex = shipsElements.indexOf(element)
        if (shipIndex < 0) return
        soundManager.bulletBurst()
        enemyDrawer.removeShip(shipIndex)
    }

    private fun stopBullet(bullet: Bullet) {
        bullet.canMoveFurther = false
    }

    private fun removeView(element: Element) {
        val activity = container.context as Activity
        activity.runOnUiThread {
            container.removeView(activity.findViewById(element.viewId))
        }
    }

    private fun getCoordinatesForTopOrBottomDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        val rightCell = leftCell + CELL_SIZE
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        return listOf(
                Coordinate(topCoordinate, leftCell),
                Coordinate(topCoordinate, rightCell)
        )
    }

    private fun getCoordinatesForLeftOrRightDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val topCell = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        val bottomCell = topCell + CELL_SIZE
        val leftCoordinate = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        return listOf(
                Coordinate(topCell, leftCoordinate),
                Coordinate(bottomCell, leftCoordinate)
        )
    }

    private fun createBullet(myShip: View, currentDirection: Direction): ImageView {
        return ImageView(container.context)
                .apply {
                    this.setImageResource(R.drawable.bullet)
                    this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)
                    val bulletCoordinate = getBulletCoordinates(this, myShip, currentDirection)
                    (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
                    (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
                    this.rotation = currentDirection.rotation
                }
    }

    private fun getBulletCoordinates(
            bullet: ImageView,
            myShip: View,
            currentDirection: Direction
    ): Coordinate {
        val shipLeftTopCoordinate = Coordinate(myShip.top, myShip.left)
        return when (currentDirection) {
            Direction.UP ->
                Coordinate(
                        top = shipLeftTopCoordinate.top - bullet.layoutParams.height,
                        left = getDistanceToMiddleOfShip(shipLeftTopCoordinate.left, bullet.layoutParams.width)
                )
            Direction.DOWN ->
                Coordinate(
                        top = shipLeftTopCoordinate.top + myShip.layoutParams.height,
                        left = getDistanceToMiddleOfShip(shipLeftTopCoordinate.left, bullet.layoutParams.width)
                )
            Direction.LEFT ->
                Coordinate(
                        top = getDistanceToMiddleOfShip(shipLeftTopCoordinate.top, bullet.layoutParams.height),
                        left = shipLeftTopCoordinate.left - bullet.layoutParams.width
                )
            Direction.RIGHT ->
                Coordinate(
                        top = getDistanceToMiddleOfShip(shipLeftTopCoordinate.top, bullet.layoutParams.height),
                        left = shipLeftTopCoordinate.left + myShip.layoutParams.width
                )
        }
    }

    private fun getDistanceToMiddleOfShip(startCoordinate: Int, bulletSize: Int): Int {
        return startCoordinate + (CELL_SIZE - bulletSize / 2)
    }
}
