package com.example.battlecity.drawers

import android.widget.FrameLayout
import com.example.battlecity.activities.CELL_SIZE
import com.example.battlecity.GameCore

import com.example.battlecity.activities.HALF_WIDTH_OF_CONTAINER
import com.example.battlecity.activities.VERTICAL_MAX_SIZE
import com.example.battlecity.enums.Direction

import com.example.battlecity.models.Coordinate
import com.example.battlecity.models.Element
import com.example.battlecity.models.Ship

import com.example.battlecity.utils.checkIfChanceBiggerThanRandom
import com.example.battlecity.utils.drawElement

import com.example.battlecity.enums.Material
import com.example.battlecity.sounds.MainSoundPlayer


private const val MAX_ENEMY_AMOUNT = 20

class EnemyDrawer(
        private val container: FrameLayout,
        private val elements: MutableList<Element>,
        private val soundManager: MainSoundPlayer,
        private val gameCore: GameCore
) {
    private val respawnList: List<Coordinate>
    private var enemyAmount = 0
    private var currentCoordinate: Coordinate
    val ships = mutableListOf<Ship>()
    lateinit var bulletDrawer: BulletDrawer
    private var gameStarted = false
    private var enemyMurders = 0

    init {
        respawnList = getRespawnList()
        currentCoordinate = respawnList[0]
    }

    private fun getRespawnList(): List<Coordinate> {
        val respawnList = mutableListOf<Coordinate>()
        respawnList.add(Coordinate(0, 0))
        respawnList.add(Coordinate(0, HALF_WIDTH_OF_CONTAINER - CELL_SIZE))
        respawnList.add(Coordinate(0, VERTICAL_MAX_SIZE - 2 * CELL_SIZE))
        return respawnList
    }

    fun startEnemyCreation() {
        if (gameStarted) {
            return
        }
        gameStarted = true
        Thread(Runnable {
            while (enemyAmount < MAX_ENEMY_AMOUNT) {
                if (!gameCore.isPlaying()) {
                    continue
                }
                drawEnemy()
                enemyAmount++
                Thread.sleep(3000)
            }
        }).start()
        moveEnemyShips()
    }

    private fun drawEnemy() {
        var index = respawnList.indexOf(currentCoordinate) + 1
        if (index == respawnList.size) {
            index = 0
        }
        currentCoordinate = respawnList[index]
        val enemyShip = Ship(
                Element(
                        material = Material.ENEMY_SHIP,
                        coordinate = currentCoordinate
                ), Direction.DOWN, this
        )
        enemyShip.element.drawElement(container)
        ships.add(enemyShip)
    }

    private fun moveEnemyShips() {
        Thread(Runnable {
            while (true) {
                if (!gameCore.isPlaying()) {
                    continue
                }
                goThroughAllShips()
                Thread.sleep(400)
            }
        }).start()
    }

    private fun goThroughAllShips() {
        if (ships.isNotEmpty()) {
            soundManager.shipMove()
        } else {
            soundManager.shipStop()
        }
        ships.toList().forEach {
            it.move(it.direction, container, elements)
            if (checkIfChanceBiggerThanRandom(10)) {
                bulletDrawer.addNewBulletForShip(it)
            }
        }
    }

    private fun isAllShipsDestroyed(): Boolean {
        return enemyMurders == MAX_ENEMY_AMOUNT
    }

    fun getPlayerScore() = enemyMurders * 100

    fun removeShip(shipIndex: Int) {
        ships.removeAt(shipIndex)
        enemyMurders++
        if (isAllShipsDestroyed()) {
            gameCore.playerWon(getPlayerScore())
        }
    }
}
