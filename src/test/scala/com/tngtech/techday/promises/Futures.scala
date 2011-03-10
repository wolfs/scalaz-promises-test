package com.tngtech.techday.promises

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.util.concurrent.{Future, Callable, Executors}
import compat.Platform

/**
 * @author wolfs
 */

class Futures extends FunSuite with ShouldMatchers {
  implicit def funToCallable[A](a: => A): Callable[A] = {
    new Callable[A] { override def call = { a } }
  }

  test("ExecutorService turns Callables to Futures") {
    val executor = Executors.newFixedThreadPool(3);
    try {
      val before: Long = Platform.currentTime

      // Careful: Implicits have problems with call-by-value parameters
      val e1: Future[Int] = executor.submit({Thread.sleep(50); 5}: Int)
      val e2: Int = {Thread.sleep(60); 6}

      // e1 and e2 are evaluated concurrently
      val result: Int = e1.get + e2

      val after: Long = Platform.currentTime

      result should equal (11)
      (after - before) should be < 75L;
    } finally {
      executor.shutdownNow
    }
  }
}