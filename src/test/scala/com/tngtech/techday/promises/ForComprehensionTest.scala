package com.tngtech.techday.promises

import org.scalatest.fixture.FixtureFunSuite
import com.tngtech.scalatest.utils.ExecutorFixture
import java.util.concurrent.Executors
import scalaz.Scalaz
import scalaz.concurrent.{Promise, Strategy}

/**
 * @author wolfs
 */

class ForComprehensionTest extends FixtureFunSuite {
  type FixtureParam = Strategy

  override def withFixture(test: OneArgTest) {
    import ExecutorFixture._
    val executor = Executors.newFixedThreadPool(4)
    runWithExecutor(executor)(test)
  }

  test("Can do for for a promises") { implicit strategy =>
    import Scalaz._

    val x: Promise[Int] = for {
      y <- promise(5)(Strategy.Sequential)
      x <- promise(4)(Strategy.Id)
    } yield { x + 3}
  }


}