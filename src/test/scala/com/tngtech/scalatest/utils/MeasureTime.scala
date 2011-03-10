package com.tngtech.scalatest.utils

import compat.Platform


/**
 * @author wolfs
 */

trait MeasureTime {
  def measureTime[A,B](block: A => B): (A => (B,Long)) = {
    input =>
      val before = Platform.currentTime;
      val result = block(input)
      val after = Platform.currentTime
      (result, after - before)
  }

  def measureTime[A](block: => A): (A, Long) = {
      val before = Platform.currentTime;
      val result = block
      val after = Platform.currentTime
      (result, after - before)
  }

}

object MeasureTime extends MeasureTime