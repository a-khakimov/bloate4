package org.github.ainr.bloate4.infrastructure.logging

import org.slf4j
import org.slf4j.LoggerFactory

trait LazyLogging {
  implicit lazy val logger: slf4j.Logger = LoggerFactory.getLogger(getClass.getName)
}