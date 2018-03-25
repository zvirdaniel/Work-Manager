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
	var editCellFinishNow: Boolean? = false // Used in the row editor, fixes a bug in the JavaFX platform
	var editCellCancelNow: Boolean? = false // Used in the row editor, cancels editing a row completely
	val appTitle = "Work Manager"
	var services: HostServices? = null // Used in the about dialog to open a link in a web browser
	val maskerPane = MaskerPane() // Used to block the user interface
	val zone: ZoneId = ZoneId.of("Europe/Prague") // This zone is used in all DateTime/Instant conversions
	lateinit var primaryStage: Stage // To make primary stage accessible everywhere
	private val tableViewControllers = mutableListOf<TableViewController>() // Contains all controllers
	var currentTab: Int = -1 // Currently selected tab, gets changed automatically
	lateinit var mainController: MainController // Month controllers need to hook to the main controller in order to recalculate wages
	val currentMonth: Int // Currently selected month, gets calculated automatically
		get() = currentTab + 1

	init {
		maskerPane.visibleProperty().value = false
	}

	/**
	 * @param tabIndex implicitly set to the currently selected tab
	 * @return controller instance of the given tab
	 */
	fun getTableViewController(tabIndex: Int = currentTab) = tableViewControllers[tabIndex]

	/**
	 * Initializes all controllers with data from the DataHolder
	 * @param controller instance to be added into the list, can be retrieved later
	 */
	fun addTableViewController(controller: TableViewController) {
		tableViewControllers.add(controller)

		if (tableViewControllers.count() == 12) {
			tableViewControllers.forEachIndexed { i, c ->
				c.table.items = MemoryManager.workYear.months[i + 1]?.sessions
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