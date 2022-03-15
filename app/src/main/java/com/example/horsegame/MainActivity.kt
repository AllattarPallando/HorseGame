package com.example.horsegame

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.test.runner.screenshot.ScreenCapture
import androidx.test.runner.screenshot.Screenshot.capture
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null

    private var mHandler: Handler? = null
    private var timeInSeconds: Long = 0
    private var gaming: Boolean = true
    private var stringShare: String = ""

    private var cellSelected_x: Int = 0
    private var cellSelected_y: Int = 0

    private var nextLevel: Boolean = false
    private var level: Int = 1
    private var levelMoves: Int = 0
    private var scoreLevel: Int = 1

    private var movesRequired: Int = 0
    private var moves: Int = 0

    private var lives: Int = 1
    private var scoreLives: Int = 1

    private var options: Int = 0

    private var bonus: Int = 0
    private var withBonus: Int = 0

    private var checkMovement: Boolean = true

    private var nameColorBlack: String = "black_cell"
    private var nameColorWhite: String = "white_cell"

    private lateinit var board: Array<IntArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initScreenGame()
        startGame()
    }

    private fun initScreenGame(){
        setSizeBoard()
        hideMessage(false)
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
    private fun hideMessage(start: Boolean){
        var lyMessage: LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE

        if(start) startGame()
    }

    fun launchAction(v: View){
        hideMessage(true)
    }

    fun launchShareGame(v: View){
        shareGame()
    }
    private fun shareGame(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        var ssc: ScreenCapture = capture(this)
        bitmap = ssc.bitmap

        if(bitmap != null){
            var idGame: String = SimpleDateFormat("yy/MM/dd").format(Date())
            idGame = idGame.replace(":", "")
            idGame = idGame.replace("/", "")

            val path = saveImage(bitmap, "${idGame}.jpg")
            val bmpUri = Uri.parse(path)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT, stringShare)
            shareIntent.type = "image/png"

            val finalShareIntent = Intent.createChooser(shareIntent, "Select the app yo want to share the game to")
            finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(finalShareIntent)
        }

    }
    private fun saveImage(bitmap: Bitmap?, fileName: String): String?{
        if(bitmap == null) return  null

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
            val contentValues = ContentValues().apply{
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots")
            }

            val uri: Uri? = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if(uri != null){
                this.contentResolver.openOutputStream(uri).use{
                    if(it == null) return@use
                    bitmap.compress(Bitmap.CompressFormat.PNG, 35, it)
                    it.flush()
                    it.close()

                    //Add pic to gallery
                    MediaScannerConnection.scanFile(this, arrayOf(uri.toString()), null, null)
                }
            }
            return uri.toString()
        }

        val filePath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Screenshots").absolutePath
        val dir: File = File(filePath)
        if(!dir.exists()) dir.mkdirs()
        val file: File = File(dir, fileName)
        val fOut: FileOutputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()

        //Add pic to gallery
        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null, null)
        return filePath
    }

    fun checkCellClicked(v: View){
        var name: String = v.tag.toString()
        var x: Int = name.subSequence(1,2).toString().toInt()
        var y: Int = name.subSequence(2,3).toString().toInt()

        checkCell(x, y)
    }
    private fun checkCell(x: Int, y: Int){
        var checkTrue: Boolean = true

        if(checkMovement){
            var dif_x: Int = x - cellSelected_x
            var dif_y: Int = y - cellSelected_y
            checkTrue = false

            if(dif_x == 1 && dif_y == 2) checkTrue = true // right - top long
            if(dif_x == 1 && dif_y == -2) checkTrue = true // right - bottom long
            if(dif_x == 2 && dif_y == 1) checkTrue = true // right long - top
            if(dif_x == 2 && dif_y == -1) checkTrue = true // right long - bottom
            if(dif_x == -1 && dif_y == 2) checkTrue = true // left - top long
            if(dif_x == -1 && dif_y == -2) checkTrue = true // left - bottom long
            if(dif_x == -2 && dif_y == 1) checkTrue = true // left long - top
            if(dif_x == -2 && dif_y == -1) checkTrue = true // left long - bottom
        } else {
            if(board[x][y] != 1){
                bonus--
                var tvBonusData: TextView = findViewById(R.id.tvBonusData)
                tvBonusData.text = " + $bonus"

                if(bonus == 0) tvBonusData.text = ""
            }
        }

        if(board[x][y] == 1) checkTrue = false

        if(checkTrue) selectCell(x, y)

    }
    private fun selectCell(x: Int, y: Int){
        moves--
        var tvMovesData: TextView = findViewById(R.id.tvMovesData)
        tvMovesData.text = moves.toString()

        growProgressBonus()

        if(board[x][y] == 2){
            bonus++
            var tvBonusData: TextView = findViewById(R.id.tvBonusData)
            tvBonusData.text = " + $bonus"
        }

        board[x][y] = 1
        paintHorseCell(cellSelected_x, cellSelected_y, "previous_cell")

        cellSelected_x = x
        cellSelected_y = y

        clearOptions()

        paintHorseCell(x, y, "selected_cell")
        checkMovement = true
        checkOptions(x, y)

        if(moves > 0){
            checkNewBonus()
            checkGameOver()
        }
        else showMessage("You Win!", "Next Level", false)
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
    private fun clearBoard(){
        var iv: ImageView

        var colorBlack: Int = ContextCompat.getColor(this, resources.getIdentifier(nameColorBlack, "color", packageName))
        var colorWhite: Int = ContextCompat.getColor(this, resources.getIdentifier(nameColorWhite, "color", packageName))

        for(i in 0..7){
            for(j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j", "id", packageName))
                iv.setImageResource(0)

                if(checkColorCell(i, j) == "black") iv.setBackgroundColor(colorBlack)
                else iv.setBackgroundColor(colorWhite)
            }
        }
    }
    private fun setFirstPosition(){
        var x: Int = 0
        var y: Int = 0

        var firstPosition: Boolean = false
        while(!firstPosition){
            x = (0..7).random()
            y = (0..7).random()

            if(board[x][y] == 0) firstPosition = true
            checkOptions(x, y)
            if(options == 0) firstPosition = false
        }

        cellSelected_x = x
        cellSelected_y = y

        selectCell(x, y)
    }

    private fun setLevel(){
        if(nextLevel){
            level++
            /*if(!premium) setLives()
            else{
                editor.apply{
                    putInt("LEVEL", level!!)
                }.apply()
            }*/
        }
        //lives = level
        else{
            //if(!premium){
                lives--
                if(lives < 1){
                    level = 1
                    lives = 1
                }
            //}
        }
    }
    private fun setLevelParameters(){
        var tvLiveData: TextView = findViewById(R.id.tvLiveData)
        tvLiveData.text = lives.toString()

        scoreLives = lives

        var tvLevelNumber: TextView = findViewById(R.id.tvLevelNumber)
        tvLevelNumber.text = level.toString()
        scoreLevel = level

        bonus = 0
        var tvBonusData: TextView = findViewById(R.id.tvBonusData)
        tvBonusData.text = ""

        setLevelMoves()
        moves = levelMoves

        movesRequired = setMovesRequired()
    }
    private fun setLevelMoves(){
        when(level){
            1-> levelMoves = 64
            2-> levelMoves = 56
            3-> levelMoves = 32
            4-> levelMoves = 16
            5-> levelMoves = 48
        }
    }
    private fun setMovesRequired(): Int{
        var movesRequired: Int = 0

        when(level){
            1-> movesRequired = 8
            2-> movesRequired = 10
            3-> movesRequired = 12
            4-> movesRequired = 10
            5-> movesRequired = 10
        }
        return movesRequired
    }

    private fun setBoardLevel(){
        when(level){
            2-> paintLevel_2()
            3-> paintLevel_3()
            4-> paintLevel_4()
            5-> paintLevel_5()
        }
    }
    private fun paintColumn(column: Int){
        for(i in 0..7){
            board[column][i] = 1
            paintHorseCell(column, i, "previous_cell")
        }
    }
    private fun paintLevel_2(){
        paintColumn(6)
    }
    private fun paintLevel_3(){
        for(i in 0..7){
            for(j in 4..7){
                board[j][i] = 1
                paintHorseCell(j, i, "previous_cell")
            }
        }
    }
    private fun paintLevel_4(){
        paintLevel_3(); paintLevel_5()
    }
    private fun paintLevel_5(){
        for(i in 0..3){
            for(j in 0..3){
                board[j][i] = 1
                paintHorseCell(j, i, "previous_cell")
            }
        }
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

    private fun clearOption(x: Int, y: Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if(checkColorCell(x, y) == "black")
            iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(nameColorBlack, "color", packageName)))
        else
            iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(nameColorWhite, "color", packageName)))

        if(board[x][y] == 1)
            iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier("previous-cell", "color", packageName)))
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
    private fun paintOption(x: Int, y:Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if(checkColorCell(x, y) == "black") iv.setBackgroundResource(R.drawable.option_black)
        else iv.setBackgroundResource(R.drawable.option_white)
    }
    private fun paintAllOptions(){
        for (i in 0..7){
            for(j in 0..7){
                if (board[i][j] != 1) paintOption(i, j)
                if (board[i][j] == 0) board[i][j] = 9
            }
        }
    }

    private fun checkGameOver(){
        if(options == 0){
            if(bonus > 0) {
                checkMovement = false
                paintAllOptions()
            }
            else showMessage("Game Over", "Try again!", true)

        }
    }
    private fun showMessage(title: String, action: String, gameOver: Boolean){
        gaming = false
        nextLevel != gameOver

        var lyMessage: LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.VISIBLE

        var tvTitleMessage: TextView = findViewById(R.id.tvTitleMessage)
        tvTitleMessage.text = title

        var tvTimeData: TextView = findViewById(R.id.tvTimeData)
        var score: String = ""
        if(gameOver){
            score = "Score: " + (levelMoves-moves) + "/" + levelMoves
            stringShare = "This game makes me sick!!" + score +" http://jotajotavm.com/retocaballo"
        }
        else{
            score = tvTimeData.text.toString()
            stringShare = "Let's Go!! New challenge completed. Level: $level (" + score +") http://jotajotavm.com/retocaballo"
        }

        var tvScoreMessage: TextView = findViewById(R.id.tvScoreMessage)
        tvScoreMessage.text = score

        var tvAction: TextView = findViewById(R.id.tvAction)
        tvAction.text = action
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
                paintOption(option_x, option_y)

                if(board[option_x][option_y] == 0) board[option_x][option_y] = 9
            }
        }
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

    private fun resetTime(){
        mHandler?.removeCallbacks(chronometer)
        timeInSeconds = 0

        var tvTimeData: TextView = findViewById(R.id.tvTimeData)
        tvTimeData.text = "00:00"
    }
    private fun startTime(){
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }
    private var chronometer: Runnable = object: Runnable{
        override fun run() {
            try {
                if(gaming){
                    timeInSeconds++
                    updateStopWatchView(timeInSeconds)
                }
            } finally {
                mHandler!!.postDelayed(this, 1000L)
            }
        }
    }
    private fun updateStopWatchView(timeInSeconds: Long){
        val formattedTime = getFormattedStopWatch((timeInSeconds * 1000))
        var tvTimeData: TextView = findViewById(R.id.tvTimeData)
        tvTimeData.text = formattedTime
    }
    private fun getFormattedStopWatch(ms: Long) : String{
        var milliseconds = ms
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds((milliseconds))

        return "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }

    private fun startGame(){
        setLevel()
        setLevelParameters()

        resetBoard()
        clearBoard()

        setBoardLevel()
        setFirstPosition()

        resetTime()
        startTime()
        gaming = true
    }

}