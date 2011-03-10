package com.tngtech.techday.promises

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scalaz.Scalaz

/**
 * @author wolfs
 */

class EitherTest extends FunSuite with ShouldMatchers {
  test("Map should work on Right") {
    val newRight = {
      import Scalaz._
      val right: Either[Nothing,Int] = Right(5)
      right.map(_ + 3)
    }
    newRight match {
      case Right(x) => x should equal(8)
      case Left(_) => throw new IllegalStateException
    }
  }

  test("CatchOrReturn works") {
    val ret = catchOrReturn(failOrFour(false))
    assert(ret.left.get.isInstanceOf[IllegalStateException])
  }

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