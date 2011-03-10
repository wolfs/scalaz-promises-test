package com.tngtech.scalatest.utils
import scalaz.concurrent.Strategy
import java.util.concurrent.{TimeUnit, ExecutorService}

/**
 * @author wolfs
 */

trait ExecutorFixture {
  def runWithExecutor[A](executor: ExecutorService)(block: Strategy => A):A = {
    try {
      val strategy = Strategy.Executor(executor)
      block(strategy)
    } finally {
      executor.shutdown;
      executor.awaitTermination(60, TimeUnit.SECONDS)
    }
  }
}

object ExecutorFixture extends ExecutorFixture