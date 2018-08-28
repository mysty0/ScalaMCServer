package com.scalamc.models.world

import com.scalamc.models.enums.Difficulty.Difficulty
import com.scalamc.models.enums.{Difficulty, Dimension, LevelType}
import com.scalamc.models.enums.Dimension
import com.scalamc.models.enums.Dimension.Dimension
import com.scalamc.models.enums.LevelType.LevelType

case class WorldInfo(name: String = "",
                     dimension: Dimension = Dimension.Overworld,
                     difficulty: Difficulty = Difficulty.Normal,
                     levelType: LevelType = LevelType.Default)


