package model

/** The model.
 */
class Model extends PublicModel {

  private var score: Int = 0

  override def getCurrentScore: Int = this.score

  override def resetCurrentScore(): Unit = this.score = 0

  override def restartGame(): Unit = {
    this.resetCurrentScore()
  }
}
