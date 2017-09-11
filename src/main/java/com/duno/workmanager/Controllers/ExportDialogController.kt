package com.duno.workmanager.Controllers

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import org.controlsfx.control.RangeSlider
import java.net.URL
import java.time.ZoneId
import java.util.*

// TODO: App did not show opened file in title

class ExportDialogController : Initializable {
    @FXML lateinit var monthPane: AnchorPane
    @FXML lateinit var filePane: AnchorPane
    @FXML lateinit var onlyOneMonthCheckbox: CheckBox

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val button = Button("Vyberte soubor")

        button.onAction = EventHandler {
            val chooser = FileChooser()
            chooser.title = "Vyber soubor"
            chooser.initialFileName = "K2 Práce"
            chooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Excel Tabulka", "*.xlsx"))
        }

        filePane.children.setAll(button)

        setAndShowChoiceBox()

        onlyOneMonthCheckbox.selectedProperty().addListener { _, _, value ->
            when (value) {
                true -> setAndShowChoiceBox()
                false -> setAndShowSlider()
            }
        }
    }

    private fun setAndShowSlider() {
        val slider = RangeSlider(1.0, 12.0, 1.0, 12.0)
        slider.showTickMarksProperty().set(true)
        slider.showTickLabelsProperty().set(true)
        slider.snapToTicksProperty().set(true)
        slider.minorTickCount = 0
        slider.majorTickUnit = 1.0
        slider.blockIncrement = 1.0

        setAllAnchors(slider, 0.0)

        monthPane.children.setAll(slider)
    }

    private fun setAllAnchors(node: Node, value: Double) {
        AnchorPane.setTopAnchor(node, value)
        AnchorPane.setBottomAnchor(node, value)
        AnchorPane.setRightAnchor(node, value)
        AnchorPane.setLeftAnchor(node, value)
    }

    private fun setAndShowChoiceBox() {
        val choices = FXCollections.observableArrayList<String>(
                "Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec",
                "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
        )

        val choiceBox = ChoiceBox<String>(choices)
        choiceBox.selectionModel.select(Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().month.value - 1)

        AnchorPane.setTopAnchor(choiceBox, 20.0)
        AnchorPane.setBottomAnchor(choiceBox, 20.0)

        monthPane.children.setAll(choiceBox)
    }
}