package model.entities

import com.badlogic.gdx.physics.box2d.Body

class AttackEntity(private var body: Body, private val size: (Float, Float)) extends MobileEntityImpl(body, size) {

}
