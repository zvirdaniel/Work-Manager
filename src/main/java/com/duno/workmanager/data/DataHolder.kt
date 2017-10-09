package com.duno.workmanager.data

import com.duno.workmanager.controllers.TableViewController
import javafx.application.HostServices
import javafx.event.EventHandler
import javafx.stage.Stage
import org.controlsfx.control.MaskerPane
import java.time.ZoneId

/**
 * Holds static variables
 */
object DataHolder {
    var services: HostServices? = null // Used in about dialog to openFile a link in a web browser
    val maskerPane = MaskerPane() // Used to block the UI
    val zone: ZoneId = ZoneId.systemDefault() // This zone is used in all DateTime/Instant conversion
    lateinit var primaryStage: Stage
    private val tableViewControllers = mutableListOf<TableViewController>()

    init {
        maskerPane.visibleProperty().value = false
    }

    /**
     * @return controller instance of a given month
     */
    fun getTableViewController(index: Int): TableViewController {
        return tableViewControllers[index]
    }

    /**
     * @param controller instance to be added into the list, can be retrieved later
     */
    fun addTableViewController(controller: TableViewController) {
        tableViewControllers.add(controller)

        if (tableViewControllers.count() == 12) {
            tableViewControllers.forEachIndexed { i, c ->
                c.table.items = VisibleData.visibleDataMap[i + 1]
            }

            tableViewControllers.forEach { c ->
                val table = c.table
                table.onSort = EventHandler {
                    if (table.items.isNotEmpty()) {
                        table.scrollTo(0)
                    }
                }
            }
        }
    }
}