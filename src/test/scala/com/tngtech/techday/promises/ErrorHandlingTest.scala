package com.tngtech.techday.promises

import scalaz.Scalaz
import org.scalatest.matchers.ShouldMatchers
import scalaz.concurrent.{Strategy, Promise}
import org.scalatest.TestFailedException
import java.util.concurrent._
import org.scalatest.fixture.FixtureFunSuite
import com.tngtech.scalatest.utils.{ExecutorFixture, TimeoutTest}

/**
 * @author wolfs
 */

class ErrorHandlingTest extends FixtureFunSuite with ShouldMatchers with TimeoutTest {

  type FixtureParam = Strategy

  override def withFixture(test: OneArgTest) {
    import ExecutorFixture._
    val executor = Executors.newFixedThreadPool(4)
    runWithExecutor(executor)(test)
  }

  test("Exception should be thrown when get is called") { implicit strategy =>
    import Scalaz._
    val failingPromise: Promise[Int] = promise(failOrFour(false))
    evaluating{ failingPromise.get } should produce [IllegalStateException]
  }

  test("If one promise throws an exception it does not trigger depending promises") { implicit strategy =>
    import Scalaz._
    val failingPromise: Promise[Int] = promise(failOrFour(false,0))
    val consecPromise = failingPromise.flatMap(a => promise(a + 7))
    intercept[IllegalStateException](failingPromise.get)
    intercept[TestFailedException] {
      withTimeout(1000) (consecPromise.get)
    }
  }

  test("Use either to encapsulate failure") { implicit strategy =>
    import Scalaz._
    val failingPromise: Promise[Either[Throwable,Int]] = promise(catchOrReturn(failOrFour(false,500)))
    val b: Promise[Either[Throwable,Int]] = failingPromise.flatMap(a => promise(a.map(_ + 7)))
    val result = b.get
    result match {
      case Left(x: IllegalStateException) => true
      case _ => throw new IllegalStateException("Should have been an exception!")
    }
  }

//  test("Handling of Exception in future invokations should work") {
//    implicit val executor = Executors.newFixedThreadPool(2)
//    val handledPromise = { import Scalaz._
//      implicit val strategy = Strategy.Executor(executor)
//
//      val failingPromise: Promise[Int] = promise(failOrFour(false))
//      for (
//        x <- failingPromise
//      ) yield {
//        try {
//          x
//        } catch {
//          case e: IllegalStateException => 7
//        }
//      }
//    }
//    handledPromise.get should equal (7)
//    executor.shutdown
//    executor.awaitTermination(5, TimeUnit.SECONDS)
//  }

  def catchOrReturn[A](block: => A):Either[Throwable,A] = {
    try {
      val a: A = block
      Right(a)
    } catch {
      case e: Throwable => Left(e)
    }
  }

  def failOrFour(success: Boolean, timeout: Int = 500) = {
    if (success) {
      4
    } else {
      Thread.sleep(timeout)
      throw new IllegalStateException("Test")
    }
  }

}