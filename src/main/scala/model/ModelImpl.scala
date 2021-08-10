package model

trait Model {

  def update()
}

class ModelImpl extends Model {

  override def update(): Unit = {
    println("MODEL update")
  }
}
