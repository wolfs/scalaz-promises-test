package com.tngtech.techday.promises

import org.scalatest.FunSuite
import scalaz.concurrent.{Promise, Strategy}
import org.scalatest.matchers.ShouldMatchers
import java.util.concurrent.{TimeUnit, ExecutorService, Executors}
import com.tngtech.scalatest.utils.{ExecutorFixture, MeasureTime}

/**
 * @author wolfs
 */

class PromiseBasic extends FunSuite with ShouldMatchers with MeasureTime with ExecutorFixture {
  import scalaz.Scalaz._

  test("Runs parallel (with flatMap)") {
    implicit val executor = Executors.newFixedThreadPool(4)
    try {
      implicit val strategy = Strategy.Executor

      val (res, time) = measureTime {
        val promiseA: Promise[Int] = promise({ Thread.sleep(400); 3})
        val promiseB: Promise[Int] = promise({Thread.sleep(600); 4})

        val sumPromise: Promise[Int] =
          promiseA.flatMap(a => promiseB.map(b => a + b))
        sumPromise.get
      }

      res should equal (7)
      time should be <= 800L
    } finally {
      executor.shutdownNow()
      executor.awaitTermination(60, TimeUnit.SECONDS)
    }
  }

  test("Runs parallel (with for)") {
    val (res: Int, time) = runAndMeasureTime(Executors.newFixedThreadPool(4)) {
      implicit strategy =>
        val promiseA: Promise[Int] = promise({ Thread.sleep(400); 3})
        val promiseB: Promise[Int] = promise({Thread.sleep(600); 4})

        val sumPromise: Promise[Int] =
          for {
            x <- promiseA
            y <- promiseB
          } yield { x + y}

        sumPromise.get
    }

    res should equal (7)
    time should be <= 800L

  }

  test("Runs parallel (with applicative)") {
    val (res: Int, time) = runAndMeasureTime(Executors.newFixedThreadPool(4)) {
      implicit strategy =>
        val promiseA: Promise[Int] = promise({ Thread.sleep(400); 3})
        val promiseB: Promise[Int] = promise({Thread.sleep(600); 4})
        val sumPromise = (promiseA <**> promiseB) (_ + _)

        sumPromise.get
    }

    res should equal (7)
    time should be <= 800L
  }

  test("Don't use lazy vals, since they synchronize on this") {
    val (res: Int, time) = runAndMeasureTime(Executors.newFixedThreadPool(4)) {
      implicit strategy =>
        lazy val a = {Thread.sleep(500); 5}
        lazy val b = {Thread.sleep(500); 7}
        val calcA: Promise[Int] = promise(a)
        val calcB: Promise[Int] = promise(b)
        val sumPromise: Promise[Int] =
          for ( x <- calcA; y <- calcB) yield { x + y}
        sumPromise.get
    }
    res should be (12)
    time should be >= 800L
  }

  def runAndMeasureTime[A](executor: ExecutorService): ((Strategy => A) => (A,Long)) =
    (runWithExecutor[(A,Long)](executor) _) compose (measureTime _)

}