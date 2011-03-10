package com.tngtech.techday.promises

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scalaz.concurrent.Strategy
import com.tngtech.scalatest.utils.MeasureTime

/**
 * @author wolfs
 */

class StrategyTest extends FunSuite with ShouldMatchers with MeasureTime {

  def workUnit = {
    Thread.sleep(50)
    5
  }

  def measureStrategyInitAndRetrieve(strategy: Strategy): (Long,Long) = {
    val (res: (() => Int), time) = measureTime {
      strategy(workUnit);
    }
    val (res2,time2) = measureTime {
      res()
    }
    (time, time2)
  }

  test("Identity should not execute immediately") {
    val (time, time2) = measureStrategyInitAndRetrieve(Strategy.Id)
    time should be <= (15L)
    time2 should be >= (50L)

  }

  test("Sequential should execute immediately") {
    val (time, time2) = measureStrategyInitAndRetrieve(Strategy.Sequential)
    time should be >= (50L)
    time2 should be <= (15L)
  }

  test("Naive should execute everything parallel") {
    val (res, time) = measureTime {
      val res1 = Strategy.Naive(workUnit)
      val res2 = Strategy.Naive(workUnit)
      res1() + res2() }
    res should equal (10)
    time should be <= (65L)
  }

}