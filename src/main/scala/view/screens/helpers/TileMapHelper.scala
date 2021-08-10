package view.screens.helpers

import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

object TileMapHelper {

  def getMap(path: String): OrthogonalTiledMapRenderer = {
    new OrthogonalTiledMapRenderer(new TmxMapLoader().load(path), 1 / 10f)
  }

}
