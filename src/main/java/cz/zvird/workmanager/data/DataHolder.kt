package cz.zvird.workmanager.data

import cz.zvird.workmanager.controllers.MainController
import cz.zvird.workmanager.controllers.TableViewController
import javafx.application.HostServices
import javafx.event.EventHandler
import javafx.stage.Stage
import org.controlsfx.control.MaskerPane
import java.time.ZoneId

/**
 * Holds static variables accessible everywhere
 */
object DataHolder {
    var services: HostServices? = null // Used in about dialog to open a link in a web browser
    val maskerPane = MaskerPane() // Used to block the UI
    val zone: ZoneId = ZoneId.systemDefault() // This zone is used in all DateTime/Instant conversions
    lateinit var primaryStage: Stage // To make primaryStage accessible from everywhere
    private val tableViewControllers = mutableListOf<TableViewController>() // Contains all controller
    var currentTab: Int = -1 // Currently selected month, gets changed automatically
    lateinit var mainController: MainController // Month controllers need to hook to the main one in order to recalculate wages

    init {
        maskerPane.visibleProperty().value = false
    }

    /**
     * @return controller instance of currently selected month
     */
    fun getCurrentTableViewController(): TableViewController {
        return tableViewControllers[currentTab]
    }

    /**
     * Initializes all controllers with data from the DataHolder
     * @param controller instance to be added into the list, can be retrieved later
     */
    fun addTableViewController(controller: TableViewController) {
        tableViewControllers.add(controller)

        if (tableViewControllers.count() == 12) {
            tableViewControllers.forEachIndexed { i, c ->
                c.table.items = MemoryData.getMonth(i + 1)
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