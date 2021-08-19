package model.helpers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Fixture, World}
import model.entities.Entity

object WorldUtilities {
  def scaleForceVector(vector: Vector2) =
    new Vector2(vector.x * Gdx.graphics.getDeltaTime, vector.y * Gdx.graphics.getDeltaTime)

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float, entity:Entity): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      if (entity.getBody equals fixture.getBody) {
        output = true
        false // automatically stop consecutive queries
      } else
        true  // query next colliding fixture if present
    }, x1, y1, x2, y2)
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
}
