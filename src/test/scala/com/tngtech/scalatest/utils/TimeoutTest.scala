package com.tngtech.scalatest.utils

import java.util.concurrent._
import org.scalatest.TestFailedException

/**
 * @author wolfs
 */

trait TimeoutTest {
    def withTimeout[T](timeout: Int)(block: => T): T = {
    implicit def funToCallable[A](a: => A): Callable[A] = {
      new Callable[A] { override def call = { a } }
    }
    val executor: ExecutorService = Executors.newSingleThreadExecutor
    val future: Future[T] = executor.submit(block)
    try {
      future.get(timeout, TimeUnit.MILLISECONDS)
    } catch {
      case e: TimeoutException => throw new TestFailedException("Failed waiting for " + timeout + " milliseconds ", e, 0)
      case e: Throwable => throw e
    } finally {
      executor.shutdownNow
    }
  }
}

object TimeoutTest extends TimeoutTest