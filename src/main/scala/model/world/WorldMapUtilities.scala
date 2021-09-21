package model.world

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

trait WorldMapUtilities {

  def getMapRenderer(tiledMap: TiledMap): OrthogonalTiledMapRenderer

  def updateTiledMapList(seed: Int): Unit

  def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer): Unit

  def createWorldEntities(): Unit

}
