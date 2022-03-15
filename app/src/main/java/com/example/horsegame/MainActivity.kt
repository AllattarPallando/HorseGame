package com.example.horsegame

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var cellSelected_x: Int = 0
    private var cellSelected_y: Int = 0

    private var levelMoves: Int = 64
    private var movesRequired: Int = 4
    private var moves: Int = 64
    private var options: Int = 0
    private var bonus: Int = 0
    private var withBonus: Int = 0

    private var nameColorBlack: String = "black_cell"
    private var nameColorWhite: String = "white_cell"

    private lateinit var board: Array<IntArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initScreenGame()
        resetBoard()
        setFirstPosition()
    }

    fun checkCellClicked(v: View){
        var name: String = v.tag.toString()
        var x: Int = name.subSequence(1,2).toString().toInt()
        var y: Int = name.subSequence(2,3).toString().toInt()

        checkCell(x, y)
    }

    private fun checkCell(x: Int, y: Int){
        var dif_x: Int = x - cellSelected_x
        var dif_y: Int = y - cellSelected_y
        var checkTrue: Boolean = false

        if(dif_x == 1 && dif_y == 2) checkTrue = true // right - top long
        if(dif_x == 1 && dif_y == -2) checkTrue = true // right - bottom long
        if(dif_x == 2 && dif_y == 1) checkTrue = true // right long - top
        if(dif_x == 2 && dif_y == -1) checkTrue = true // right long - bottom
        if(dif_x == -1 && dif_y == 2) checkTrue = true // left - top long
        if(dif_x == -1 && dif_y == -2) checkTrue = true // left - bottom long
        if(dif_x == -2 && dif_y == 1) checkTrue = true // left long - top
        if(dif_x == -2 && dif_y == -1) checkTrue = true // left long - bottom

        if(board[x][y] == 1) checkTrue = false

        if(checkTrue) selectCell(x, y)

    }

    private fun resetBoard(){

        /* 0 = casilla vacía
         * 1 = casilla seleccionada
         * 2 = bonus
         * 9 = opción de casilla para el movimiento actual
        */

        board = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        )
    }

    private fun setFirstPosition(){
        var x: Int = 0
        var y: Int = 0

        x = (0..7).random()
        y = (0..7).random()

        cellSelected_x = x
        cellSelected_y = y

        selectCell(x, y)
    }

    private fun selectCell(x: Int, y: Int){
        moves--
        var tvMovesData: TextView = findViewById(R.id.tvMovesData)
        tvMovesData.text = moves.toString()

        growProgressBonus()

        if(board[x][y] == 2){
            bonus++
            var tvBonusData: TextView = findViewById(R.id.tvMovesData)
            tvBonusData.text = " + $bonus"
        }

        board[x][y] = 1
        paintHorseCell(cellSelected_x, cellSelected_y, "previous_cell")

        cellSelected_x = x
        cellSelected_y = y

        clearOptions()

        paintHorseCell(x, y, "selected_cell")
        checkOptions(x, y)

        if(moves > 0){
            checkNewBonus()
            //checkGameOver(x,y)
        }
        //else checkSucessfulEnd()
    }

    private fun growProgressBonus(){
        var moves_done: Int = levelMoves - moves
        var bonus_done: Int = moves_done/movesRequired
        var moves_rest: Int = movesRequired * (bonus_done)
        var bonus_grow: Int = moves_done - moves_rest

        var v: View = findViewById(R.id.vNewBonus)
        var with_bonus: Float = ((withBonus/movesRequired) * bonus_grow).toFloat()

        var height: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        var width: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, with_bonus, resources.displayMetrics).toInt()

        v.setLayoutParams(TableRow.LayoutParams(width, height))
    }

    private fun checkNewBonus(){
        if(moves % movesRequired == 0){
            var bonusCell_x: Int = 0
            var bonusCell_y: Int = 0
            var bonusCell: Boolean = false

            while (bonusCell == false){
                bonusCell_x = (0..7).random()
                bonusCell_y = (0..7).random()

                if(board[bonusCell_x][bonusCell_y] == 0) bonusCell = true
            }

            board[bonusCell_x][bonusCell_y] = 2
            paintBonusCell(bonusCell_x, bonusCell_y)
        }
    }

    private fun paintBonusCell(x: Int, y: Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        iv.setImageResource(R.drawable.bonus)
    }

    private fun clearOptions(){
        for (i in 0..7){
            for(j in 0..7){
                if(board[i][j] == 9 || board[i][j] == 2){
                    if(board[i][j] == 9) board[i][j] = 0
                    clearOption(i, j)
                }
            }
        }
    }

    private fun clearOption(x: Int, y: Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if(checkColorCell(x, y) == "black")
            iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(nameColorBlack, "color", packageName)))
        else
            iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(nameColorWhite, "color", packageName)))

        if(board[x][y] == 1)
            iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier("previous-cell", "color", packageName)))
    }

    private fun checkOptions(x: Int, y: Int){
        options = 0

        checkMove(x, y, 1, 2) // check move right - top long
        checkMove(x, y, 1, -2) // check move right - bottom long
        checkMove(x, y, 2, 1) // check move right long - top
        checkMove(x, y, 2, -1) // check move right long - bottom
        checkMove(x, y, -1, 2) // check move left - top long
        checkMove(x, y, -1, -2) // check move left - bottom long
        checkMove(x, y, -2, 1) // check move left long - top
        checkMove(x, y, -2, -1) // check move left long - bottom

        var tvOptionsData: TextView = findViewById(R.id.tvOptionsData)
        tvOptionsData.text = options.toString()
    }

    private fun checkMove( x:Int, y: Int, mov_x: Int, mov_y: Int){
        var option_x: Int = x + mov_x
        var option_y: Int = y + mov_y

        if(option_x < 8 && option_y < 8 && option_x >= 0 && option_y >= 0){
            if(board[option_x][option_y] == 0 || board[option_x][option_y] == 2){
                options++
                paintOptions(option_x, option_y)

                board[option_x][option_y] = 9
            }
        }
    }

    private fun paintOptions(x: Int, y:Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if(checkColorCell(x, y) == "black") iv.setBackgroundResource(R.drawable.option_black)
        else iv.setBackgroundResource(R.drawable.option_white)
    }

    private fun checkColorCell(x: Int, y: Int): String{
        var color: String= ""
        var blackColumn_x: Array<Int> = arrayOf(0,2,4,6)
        var blackRow_x: Array<Int> = arrayOf(1,3,5,7)

        if((blackColumn_x.contains(x) && blackColumn_x.contains(y)
                    || blackRow_x.contains(x) && blackRow_x.contains(y))) color = "black"
        else color = "white"

        return  color
    }

    private fun paintHorseCell(x: Int, y: Int, color: String){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(color, "color", packageName)))
        iv.setImageResource(R.drawable.icon)
    }

    private fun initScreenGame(){
        setSizeBoard()
        hide_message()
    }

    private fun setSizeBoard(){
        var iv: ImageView

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x

        var width_dp = (width / resources.displayMetrics.density)

        var lateralMarginsDP = 0
        val width_cell = (width_dp - lateralMarginsDP)/8
        val heigth_cell = width_cell

        withBonus = 2 * width_cell.toInt()

        for (i in 0..7){
            for (j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j", "id", packageName))

                var heigth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heigth_cell, resources.displayMetrics).toInt()
                var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width_cell, resources.displayMetrics).toInt()

                iv.layoutParams = TableRow.LayoutParams(width, heigth)
            }
        }
    }

    private fun hide_message(){
        var lyMessage: LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE
    }
}