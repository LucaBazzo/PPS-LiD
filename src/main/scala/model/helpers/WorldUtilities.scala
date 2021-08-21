package model.helpers

import com.badlogic.gdx.{Application, Gdx}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Fixture, World}
import model.Level
import model.entities.{Attack, Entity, HeroImpl}

import java.awt.geom.Point2D

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

  def checkEntityIsVisible(targetEntity:Entity, originEntity:Entity, world: World, level:Level): Boolean = {
    var isHeroVisible = false
    var count = 0
    world.rayCast((fixture:Fixture, _, _, _) => {
//      val levelEntity:Entity = level.getEntity(e => e.getBody.equals(fixture.getBody))
//      println(levelEntity)
//      levelEntity match {
//        case HeroImpl(_, _) => {
//          isHeroVisible = true
//          0
//        }
//        case Attack(_, _) => 1
//        case _ => 0
//      }

//      fixture.getFilterData().categoryBits == Application.WALL
      isHeroVisible = fixture.getBody.equals(targetEntity.getBody)
      -1
    }, originEntity.getBody.getWorldCenter, targetEntity.getBody.getWorldCenter)
    isHeroVisible
  }

  def getEntitiesDistance(entity1:Entity, entity2:Entity): Float = {
    // TODO : convertire distanza tra centri (attuale) in minima distanza tra le superfici dei body
    entity1.getBody.getWorldCenter.dst(entity2.getBody.getWorldCenter)
  }

}
