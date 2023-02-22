package com.example

import android.content.Context
import android.media.MediaPlayer

class SoundSystem {

//    init {
//        mContext = context
//    }
    companion object{
        private var BgMusic:MediaPlayer? = null
        private var sfxMusic:MediaPlayer? = null
       // private lateinit var mContext : Context

        fun playBgMusic(context: Context, audioFile : Int ){

            BgMusic = MediaPlayer.create(context, audioFile)
            BgMusic?.setVolume(0.1f,0.1f)
            BgMusic?.setOnPreparedListener{
                BgMusic?.start()
            }

            BgMusic?.setOnCompletionListener {
                BgMusic?.start()
            }
        } // Play BGM

        fun playSFX(context: Context, audioFile : Int ){

            sfxMusic = MediaPlayer.create(context, audioFile)
            sfxMusic?.setVolume(1.0f,1.0f)
            sfxMusic?.setOnPreparedListener{
                sfxMusic?.start()
            }
        } // Play SFX


        fun pauseBgMusic(){
            if (BgMusic != null) BgMusic!!.pause()
        } // Pause

        fun resumeBgMusic(){
            if (BgMusic != null) BgMusic!!.start()
        } // Resume

        fun pauseAll(){
            if (BgMusic != null) BgMusic!!.pause()
            if (sfxMusic != null) sfxMusic!!.pause()
        }
        fun resumeAll(){
            if (BgMusic != null) BgMusic!!.start()
            if (sfxMusic != null) sfxMusic!!.start()
        }


        fun stopSound() {
            if (BgMusic != null) {
                BgMusic!!.stop()
                BgMusic!!.release()
                BgMusic = null
            }
            if (sfxMusic != null) {
                sfxMusic!!.stop()
                sfxMusic!!.release()
                sfxMusic = null
            }

        }

    } // Companion
}