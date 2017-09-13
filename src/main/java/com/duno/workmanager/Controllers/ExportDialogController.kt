package com.duno.workmanager.Controllers

import com.duno.workmanager.Other.saveChooser
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Hyperlink
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import org.controlsfx.control.RangeSlider
import java.io.File
import java.net.URL
import java.time.ZoneId
import java.util.*

// TODO: App did not show opened file in title

class ExportDialogController : Initializable {
    @FXML lateinit var monthPane: AnchorPane
    @FXML lateinit var filePane: AnchorPane
    @FXML lateinit var onlyOneMonthCheckbox: CheckBox

    lateinit var exportButton: Node
    private var selectedFile: File? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        setAndShowButton()
        setAndShowChoiceBox()

        onlyOneMonthCheckbox.selectedProperty().addListener { _, _, value ->
            when (value) {
                true -> setAndShowChoiceBox()
                false -> setAndShowSlider()
            }
        }
    }

    /**
     * Creates a button that opens a file chooser, and sets it into an anchor pane
     * After selecting a file, a hyperlink will be created for the given file, and export button will be enabled
     */
    private fun setAndShowButton() {
        val button = Button("Vyberte soubor")

        button.onAction = EventHandler {
            selectAndSetFile()

            if (selectedFile != null) {
                exportButton.disableProperty().set(false)
                setAndShowLink()
            }
        }

        setTopBottomAnchors(button, 0.0)
        filePane.children.setAll(button)
        Platform.runLater { filePane.children.first().requestFocus() }
    }

    /**
     * Creates a link that opens a file chooser, and sets it into an anchor pane
     * After selecting a file, a new hyperlink will be created for the given file
     */
    private fun setAndShowLink() {
        val link = Hyperlink(selectedFile?.name)

        link.onAction = EventHandler {
            selectAndSetFile()

            if (selectedFile != null) {
                setAndShowLink()
            }
        }

        setTopBottomAnchors(link, 0.0)
        filePane.children.setAll(link)
    }

    /**
     * Opens a file chooser, and sets the given file
     */
    private fun selectAndSetFile() {
        val file = saveChooser(
                title = "Vyber soubor",
                initialFileName = "K2 Práce",
                filters = listOf(FileChooser.ExtensionFilter("Excel Tabulka", "*.xlsx")),
                extension = ".xlsx"
        )

        if (file != null) {
            selectedFile = file
        }
    }

    /**
     * Creates a month slider and sets it into anchor pane
     */
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

    /**
     * Creates a choice box and sets it into anchor pane
     */
    private fun setAndShowChoiceBox() {
        val choices = FXCollections.observableArrayList<String>(
                "Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec",
                "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
        )

        val choiceBox = ChoiceBox<String>(choices)
        choiceBox.selectionModel.select(Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().month.value - 1)

        setTopBottomAnchors(choiceBox, 20.0)

        monthPane.children.setAll(choiceBox)
    }

    /**
     * Sets all anchors for a given node to a given value
     */
    private fun setAllAnchors(node: Node, value: Double) {
        AnchorPane.setTopAnchor(node, value)
        AnchorPane.setBottomAnchor(node, value)
        AnchorPane.setRightAnchor(node, value)
        AnchorPane.setLeftAnchor(node, value)
    }

    /**
     * Sets top and bottom anchors for a given node to a given value
     */
    private fun setTopBottomAnchors(node: Node, value: Double) {
        AnchorPane.setTopAnchor(node, value)
        AnchorPane.setBottomAnchor(node, value)
    }
}