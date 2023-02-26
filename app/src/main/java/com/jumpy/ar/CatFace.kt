package com.jumpy.ar

import android.content.Context
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.jumpy.activity.Global
import com.jumpy.`object`.CatObject

class CatFace(
    augmentedFace: AugmentedFace?,
    context: Context,
) : AugmentedFaceNode(augmentedFace) {
    val catNode = CatObject()
    val mContext = context

    override fun onActivate() {
        super.onActivate()

        catNode?.setParent(this)
        catNode.initialize(mContext)
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        //if(Global.gamePaused) return

        //Move cat with our nose
        var noseX : Float? = null
        augmentedFace?.let { face ->
            val nose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            noseX = nose.tx()
        }
        //try to clamp cat to top position
//        var finalY =  Global.catPosY
//        if(finalY > 0.0)
//            finalY = 0.0f
        if(noseX != null) catNode.setPosx(noseX!!)
        catNode.setPosZ(Global.spawnPosZ)
    }
}