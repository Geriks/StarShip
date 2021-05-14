package com.example.battlecity.activities



import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.battlecity.GameCore
import com.example.battlecity.LevelStorage
import com.example.battlecity.ProgressIndicator
import com.example.battlecity.R

import com.example.battlecity.drawers.*
import com.example.battlecity.enums.Direction
import com.example.battlecity.enums.Direction.UP
import com.example.battlecity.enums.Direction.DOWN
import com.example.battlecity.enums.Direction.LEFT
import com.example.battlecity.enums.Direction.RIGHT
import com.example.battlecity.enums.Material
import com.example.battlecity.models.Coordinate
import com.example.battlecity.models.Element
import com.example.battlecity.models.Ship
import com.example.battlecity.sounds.MainSoundPlayer
import kotlinx.android.synthetic.main.activity_main.*


const val CELL_SIZE = 50
const val VERTICAL_CELL_AMOUNT = 30
const val HORIZONTAL_CELL_AMOUNT =35
const val VERTICAL_MAX_SIZE = CELL_SIZE * VERTICAL_CELL_AMOUNT
const val HORIZONTAL_MAX_SIZE = CELL_SIZE * HORIZONTAL_CELL_AMOUNT
const val HALF_WIDTH_OF_CONTAINER = VERTICAL_MAX_SIZE / 2

class MainActivity : AppCompatActivity(), ProgressIndicator {
    private var editMode = false
    private lateinit var item: MenuItem
    private var gameStarted = false
    private val playerShip by lazy {
        Ship(
                Element(
                        material = Material.PLAYER_SHIP,
                        coordinate = getPlayerShipCoordinate()
                ), UP, enemyDrawer
        )
    }

    private val bulletDrawer by lazy {
        BulletDrawer(
                container,
                elementsDrawer.elementsOnContainer,
                enemyDrawer,
                soundManager,
                gameCore
        )
    }

    private val gameCore by lazy {
        GameCore(this)
    }

    private val soundManager by lazy {
        MainSoundPlayer(this, this)
    }

    private fun getPlayerShipCoordinate() = Coordinate(
            top = HORIZONTAL_MAX_SIZE - Material.PLAYER_SHIP.height * CELL_SIZE,
            left = HALF_WIDTH_OF_CONTAINER - 8 * CELL_SIZE
    )

    private val fort = Element(
            material = Material.FORT,
            coordinate = getEagleCoordinate()
    )


    private fun getEagleCoordinate() = Coordinate(
            top = HORIZONTAL_MAX_SIZE - Material.FORT.height * CELL_SIZE,
            left = HALF_WIDTH_OF_CONTAINER - Material.FORT.width * CELL_SIZE / 2
    )

    private val gridDrawer by lazy {
        GridDrawer(container)
    }

    private val elementsDrawer by lazy {
        ElementsDrawer(container)
    }

    private val levelStorage by lazy {
        LevelStorage(this)
    }

    private val enemyDrawer by lazy {
        EnemyDrawer(
                container,
                elementsDrawer.elementsOnContainer,
                soundManager,
                gameCore
        )
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enemyDrawer.bulletDrawer = bulletDrawer
        container.layoutParams = FrameLayout.LayoutParams(
                VERTICAL_MAX_SIZE,
                HORIZONTAL_MAX_SIZE
        )
        editor_clear.setOnClickListener { elementsDrawer.currentMaterial = Material.EMPTY }
        editor_osters.setOnClickListener { elementsDrawer.currentMaterial = Material.OSTERS }
        editor_concrete.setOnClickListener { elementsDrawer.currentMaterial = Material.CONCRETE }
        editor_tuman.setOnClickListener { elementsDrawer.currentMaterial = Material.TUMAN }
        container.setOnTouchListener { _, event ->
            if (!editMode) {
                return@setOnTouchListener true
            }
            elementsDrawer.onTouchContainer(event.x, event.y)
            return@setOnTouchListener true
        }
        elementsDrawer.drawElementsList(levelStorage.loadLevel())
        elementsDrawer.drawElementsList(listOf(playerShip.element, fort))
        hideSettings()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        item = menu.findItem(R.id.menu_play)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                switchEditMode()
                true
            }
            R.id.menu_save -> {
                levelStorage.saveLevel(elementsDrawer.elementsOnContainer)
                true
            }
            R.id.menu_play -> {
                if (editMode) {
                    return true
                }
                showIntro()
                if(soundManager.areSoundsReady()) {
                    gameCore.startOrPauseTheGame()
                    if (gameCore.isPlaying()) {
                        resumeTheGame()
                    } else {
                        pauseTheGame()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun resumeTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_24)
        gameCore.resumeTheGame()
    }

    private fun showIntro() {
        if(gameStarted){
            return
        }
        gameStarted = true
        soundManager.loadSounds()
    }

    private fun pauseTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_24)
        gameCore.pauseTheGame()
        soundManager.pauseSounds()
    }

    override fun onPause() {
        super.onPause()
        pauseTheGame()
    }

    private fun switchEditMode() {
        editMode = !editMode
        if (editMode) {
            showSettings()
        } else {
            hideSettings()
        }
    }

    private fun showSettings() {
        gridDrawer.drawGrid()
        materials_container.visibility = VISIBLE
    }

    private fun hideSettings() {
        gridDrawer.removeGrid()
        materials_container.visibility = GONE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()) {
            return super.onKeyDown(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP -> onButtonPressed(UP)
            KEYCODE_DPAD_LEFT -> onButtonPressed(LEFT)
            KEYCODE_DPAD_DOWN -> onButtonPressed(DOWN)
            KEYCODE_DPAD_RIGHT -> onButtonPressed(RIGHT)
            KEYCODE_SPACE -> bulletDrawer.addNewBulletForShip(playerShip)
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onButtonPressed(direction: Direction) {
        soundManager.shipMove()
        playerShip.move(direction, container, elementsDrawer.elementsOnContainer)
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()) {
            return super.onKeyUp(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP, KEYCODE_DPAD_LEFT, KEYCODE_DPAD_DOWN, KEYCODE_DPAD_RIGHT -> onButtonReleased()
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun onButtonReleased() {
        if (enemyDrawer.ships.isEmpty()) {
            soundManager.shipStop()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == SCORE_REQUEST_CODE) {
            recreate()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showProgress() {
        container.visibility = GONE
        total_container.setBackgroundResource(R.color.gray)
        init_title.visibility = VISIBLE
    }

    override fun dismissProgress() {
        container.visibility = VISIBLE
        total_container.setBackgroundResource(R.color.black)
        init_title.visibility = GONE
        enemyDrawer.startEnemyCreation()
        soundManager.playIntroMusic()
        resumeTheGame()
    }

}
