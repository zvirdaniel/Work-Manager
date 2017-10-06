package com.duno.workmanager.Data

import com.duno.workmanager.Controllers.TableViewController
import javafx.application.HostServices
import javafx.stage.Stage
import org.controlsfx.control.MaskerPane

/**
 * Created by Daniel Zvir on 6.10.17.
 */
/**
 * Holds static variables
 */
object DataHolder {
    var services: HostServices? = null // Used in about dialog to open a link in a web browser
    val maskerPane = MaskerPane()
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
        }
    }
}