package com.tngtech.techday.promises

/**
 * @author wolfs
 */

import swing._
import event.ButtonClicked
import javax.swing.SwingUtilities
import scalaz.concurrent.{Promise, Strategy}

object SwingExample extends SimpleSwingApplication {
  def top = new MainFrame {
    val loadButton: Button = new Button() {
      text = "Load Table"
    }
    val table: Table = new Table(2, 3)
    val quitButton: Button = new Button() {
      text = "Quit"
    }
    title = "Scala Swing Example"
    preferredSize = new Dimension(500,300)
    contents = new BorderPanel {
      import BorderPanel.Position._
      add(loadButton,North)
      add(table, Center)
      add(quitButton, South)
    }

    listenTo(quitButton, loadButton)

    reactions += {
      case ButtonClicked(`loadButton`) => loadTableEntries()
      case ButtonClicked(`quitButton`) => exit()
    }

    def slowEntry[A](res: A, delay: Int): A = {
      if (SwingUtilities.isEventDispatchThread) {
        throw new IllegalStateException("Working not done in background thread")
      }
      Thread.sleep(delay)
      res
    }

    def updateEntry[A](x: Int, y: Int, res: A) = {
      if (!SwingUtilities.isEventDispatchThread) {
        throw new IllegalStateException("Updating GUI from background thread")
      }
      table(x,y) = res
    }

    def enableLoadButton(enable: Boolean) {
      if (!SwingUtilities.isEventDispatchThread) {
        throw new IllegalStateException("Updating GUI from background thread")
      }
      loadButton.enabled = enable;
    }

    val entries: List[List[(Int,Int)]] =
      List(List((1,100),(2,500),(3,50)), List((4,1000),(5,200),(6,30)))

    def loadTableEntries() {
      import scalaz.Scalaz._
      enableLoadButton(false)
      for {
        row <- 0 until table.model.getRowCount
        column <- 0 until table.model.getColumnCount
      } yield {
        table(row,column) = null
      }
      val updates: List[Promise[Unit]] =
        for {
          (list, row) <- entries.zipWithIndex
          ((res, delay), column) <- list.zipWithIndex
        } yield {
          promise(
              slowEntry(res,delay))(Strategy.SwingWorker).
                flatMap {a => promise(
                  updateEntry(row,column,a))(Strategy.SwingInvokeLater)}
        }
      val endRes = updates.traverse(identity)
      endRes.flatMap(x => promise(enableLoadButton(true))(Strategy.SwingInvokeLater))
    }

  }
}