package org.github.ainr.bloate4.services.messages

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex


object domain {

  type Message = String Refined MatchesRegex["[a-zA-Zа-яА-ЯёЁ\\d\\s:;.,`'!?\\[\\]\\(\\)\"]{1,200}"]
  //type Message = String Refined NonEmptyString

}
