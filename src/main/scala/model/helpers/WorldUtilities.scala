package model.helpers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, Fixture, World}
import model.entities.Entity

object WorldUtilities {
  def scaleForceVector(vector: Vector2) =
    new Vector2(vector.x * Gdx.graphics.getDeltaTime, vector.y * Gdx.graphics.getDeltaTime)

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float, entity:Entity): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      if (entity.getBody equals fixture.getBody)
        output = true
      !output // automatically stop consecutive queries if a match has been found
    }, x1, y1, x2, y2)
    output
  }

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float, entityBit:Short): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      // a collision with a specific entity type has occurred
      output = fixture.getFilterData.categoryBits == entityBit

      // automatically stop consecutive queries if a match has been found
      !output
    },x1, y1, x2, y2)
    output
  }

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      output = true
      false // automatically stop consecutive queries
    },x1, y1, x2, y2)
    output
  }

  def checkPointCollision(world:World, x:Float, y:Float): Boolean = {
    checkAABBCollision(world:World, x, y, x, y)
  }

  def checkPointCollision(world:World, x:Float, y:Float, entity:Entity): Boolean = {
    checkAABBCollision(world:World, x, y, x, y, entity)
  }

  def checkPointCollision(world:World, x:Float, y:Float, entityBit:Short): Boolean = {
    checkAABBCollision(world:World, x, y, x, y, entityBit)
  }

  def checkBodyIsVisible(world: World, sourceBody:Body, targetBody:Body, maxHorizontalAngle:Float=90): Boolean = {
    // Get the list of ordered fixtures (bodies) between source and target bodies
    var fixList:List[Fixture] = List.empty
    world.rayCast((fixture:Fixture, _, _, _) => {
      fixList = fixture :: fixList
      1
    }, sourceBody.getPosition, targetBody.getPosition)

    // Check if source and target bodies are obstructed by other colliding entities
    // No fixtures between target and source means that they are overlapping
    var isTargetVisible = if (fixList.size > 0) false else true
    var preemptiveStop = false
    for (fixture <- fixList if !preemptiveStop && !isTargetVisible) {
      isTargetVisible = fixture.getBody.equals(targetBody)

      // Check horizontal axis angle of source-target bodies
      if (isTargetVisible) {
        val angle = new Vector2(sourceBody.getPosition.sub(targetBody.getPosition)).angleDeg()
        isTargetVisible = ((angle <= maxHorizontalAngle || angle >= 360-maxHorizontalAngle)
          || (180-maxHorizontalAngle <= angle && 180+maxHorizontalAngle >= angle))
      }

      if ((sourceBody.getFixtureList.toArray().head.getFilterData.maskBits & fixture.getFilterData.categoryBits) != 0)
        preemptiveStop = true
    }
    isTargetVisible
  }

  def getBodiesDistance(body1:Body, body2:Body): Float = {
    // TODO : convertire distanza tra centri (attuale) in minima distanza tra le superfici dei body
    body1.getWorldCenter.dst(body2.getWorldCenter)
  }

}
