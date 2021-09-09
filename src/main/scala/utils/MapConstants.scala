package utils

object MapConstants {

  val WORLD_TOP_BORDER_NAME: String = "top-border"
  val WORLD_TOP_BORDER_OFFSET: (Integer, Integer) = (1, -48)
  val WORLD_RIGHT_BORDER_NAME: String = "right-border"
  val WORLD_RIGHT_BORDER_OFFSET: (Integer, Integer) = (135, 34)
  val WORLD_LEFT_BORDER_NAME: String = "left-border"
  val WORLD_LEFT_BORDER_OFFSET: (Integer, Integer) = (-27, 34)
  val WORLD_BOTTOM_BORDER_NAME: String = "bottom-border"
  val WORLD_BOTTOM_BORDER_OFFSET: (Integer, Integer) = (1, 50)

  val HERO_ROOM_MAP_NAME: String = "hero-room"
  val HERO_ROOM_OFFSET: (Integer, Integer) = (0,0)
  val BOSS_ROOM_MAP_NAME: String = "boss-room"
  val BOSS_ROOM_OFFSET: (Integer, Integer) = (135, 12)
  val TOP_KEY_ITEM_ROOM_NAME: String = "top-key-item-room"
  val TOP_KEY_ITEM_ROOM_OFFSET: (Integer, Integer) = (67, -48)
  val BOTTOM_KEY_ITEM_ROOM_NAME: String = "bottom-key-item-room"
  val BOTTOM_KEY_ITEM_ROOM_OFFSET: (Integer, Integer) = (67, 44)

  val INNER_BORDER_NAMES: String = "inner-border1"
  val INNER_BORDER_OFFSET: (Integer, Integer) = (13, 34)
  val INNER_ROOM_MAP_NAMES: Array[String] = Array("roomTMP", "room1", "room2", "room3", "room4", "room5", "room6", "room7", "room8")
  val INNER_ROOM_MAP_OFFSET: Array[(Integer, Integer)] = Array((13, -8), (13, 34), (54, -8), (54, 34), (95, -8), (95, 34))

}
