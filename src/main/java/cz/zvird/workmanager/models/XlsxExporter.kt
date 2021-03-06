package cz.zvird.workmanager.models

import cz.zvird.workmanager.data.MemoryManager
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

/**
 * Generates XLSX spreadsheet and saves it into a file
 * @param monthRange range between 1 and 12, each month will be a separate sheet
 * @param saveFile file to save all data into, example: result.xlsx
 * @throws IOException if file export fails
 */
fun writeYearInXlsx(saveFile: File, monthRange: IntRange) {
	if (monthRange.start < 1 || monthRange.endInclusive > 12) {
		throw IllegalArgumentException("Month range must be between 1 and 12!")
	}

	val titles = arrayOf("Datum", "Začátek práce", "Hodiny", "Popis práce")
	val monthsCzech = arrayOf("Leden", "Únor", "Březen", "Duben", "Květen",
			"Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec")
	val wb = XSSFWorkbook()

	for (monthNumber in monthRange) {
		val month = MemoryManager.getMonth(monthNumber)
		val monthName = monthsCzech[monthNumber - 1]

		val sheet = wb.createSheet() // Sheet with the current month
		wb.setSheetName(wb.getSheetIndex(sheet), monthName) // Set the sheet name
		sheet.printSetup.landscape = true
		sheet.fitToPage = true
		sheet.horizontallyCenter = true

		val titleRow = sheet.createRow(0) // Row 0
		titleRow.heightInPoints = 45F
		val titleCell = titleRow.createCell(0)
		titleCell.setCellValue(monthName) // Set the name of the given month to the cell
		titleCell.cellStyle = createMonthNameStyle(wb) // Set styling to the cell

		// Set the border of the month title cell
		val region = CellRangeAddress.valueOf("\$A$1:\$D$1")
		sheet.addMergedRegion(region) // Make cell 0 expand
		val border = BorderStyle.THIN
		val borderColor = IndexedColors.BLACK.index.toInt()
		RegionUtil.setBorderBottom(border, region, sheet)
		RegionUtil.setBottomBorderColor(borderColor, region, sheet)
		RegionUtil.setBorderTop(border, region, sheet)
		RegionUtil.setTopBorderColor(borderColor, region, sheet)
		RegionUtil.setBorderLeft(border, region, sheet)
		RegionUtil.setLeftBorderColor(borderColor, region, sheet)
		RegionUtil.setBorderRight(border, region, sheet)
		RegionUtil.setRightBorderColor(borderColor, region, sheet)

		// Create a header for every row
		val headerRow = sheet.createRow(1) // Row 1
		headerRow.heightInPoints = 40F
		for (i in titles.indices) {
			val headerCell = headerRow.createCell(i)
			headerCell.setCellValue(titles[i]) // Set title string to cell
			headerCell.cellStyle = createHeaderStyle(wb) // Set styling to cell
		}

		// Create cells for all the data
		for (rowNumber in 2..month.sessions.size + 1) {
			val row = sheet.createRow(rowNumber)
			for (column in titles.indices) {
				row.createCell(column)
			}
		}

		// Parse data into all cells
		for (dataRowNumber in month.sessions.indices) {
			val row = sheet.getRow(dataRowNumber + 2)
			val session = month.sessions[dataRowNumber]
			// Begin date
			row.getCell(0).setCellValue(session.beginDateProperty.value.format(DateTimeFormatter.ofPattern("dd. L. u")))
			// Begin time
			row.getCell(1).setCellValue(session.beginTimeProperty.value.format(DateTimeFormatter.ofPattern("HH:mm")))
			// Duration in hours
			val bigDecimal = BigDecimal((session.durationProperty.value.toMinutes().toDouble() / 60.0))
			row.getCell(2).setCellValue(bigDecimal.setScale(1, RoundingMode.HALF_UP).toDouble())
			// Description
			row.getCell(3).setCellValue(session.descriptionProperty.value)
		}

		// Style the cells with data
		for (rowNumber in 2..month.sessions.size + 1) {
			val row = sheet.getRow(rowNumber)
			for (column in titles.indices) {
				val cell = row.getCell(column)
				var cellStyle: XSSFCellStyle
				when (column) {
					3 -> cellStyle = createDescriptionStyle(wb)
					2 -> {
						cellStyle = createDataStyle(wb)
						cellStyle.dataFormat = wb.createDataFormat().getFormat("0.0")
					}
					else -> cellStyle = createDataStyle(wb)
				}
				cell.cellStyle = cellStyle
			}
		}

		// Formula, which calculates the total hours
		val formulaRow = sheet.createRow(month.sessions.size + 4)
		var cell = formulaRow.createCell(0)
		cell.setCellValue("Celkový počet hodin: ")
		cell.cellStyle = createDataStyle(wb)
		cell.cellStyle.font.bold = true

		cell = formulaRow.createCell(1)
		cell.cellFormula = "SUM(C3:C27)"
		cell.cellStyle = createDataStyle(wb)
		cell.cellStyle.dataFormat = wb.createDataFormat().getFormat("0.0#")
		cell.cellStyle.font.italic = true

		// Finally set column widths, the width is measured in units of 1/256th of a character width
		sheet.setColumnWidth(0, 30 * 256) // 30 characters wide
		sheet.setColumnWidth(3, 90 * 256)
	}

	// Write the output to a file
	try {
		val outputStream = saveFile.outputStream()
		wb.write(outputStream)
	} catch (e: IOException) {
		throw IOException("Exporting file ${saveFile.name} has failed")
	}
}

private fun createDescriptionStyle(wb: XSSFWorkbook): XSSFCellStyle {
	val style = wb.createCellStyle()

	// Font
	val descriptionFont = wb.createFont()
	descriptionFont.fontHeightInPoints = 10.toShort()
	style.setFont(descriptionFont)

	// Border
	style.setBorderTop(BorderStyle.THIN)
	style.setBorderBottom(BorderStyle.THIN)
	style.setBorderLeft(BorderStyle.THIN)
	style.setBorderRight(BorderStyle.THIN)
	style.topBorderColor = IndexedColors.BLACK.getIndex()
	style.bottomBorderColor = IndexedColors.BLACK.getIndex()
	style.leftBorderColor = IndexedColors.BLACK.getIndex()
	style.rightBorderColor = IndexedColors.BLACK.getIndex()

	// Alignment
	style.setAlignment(HorizontalAlignment.LEFT)
	style.setVerticalAlignment(VerticalAlignment.CENTER)
	return style
}

private fun createDataStyle(wb: XSSFWorkbook): XSSFCellStyle {
	val style = wb.createCellStyle()

	// Font
	val font = wb.createFont()
	font.fontHeightInPoints = 10.toShort()
	style.setFont(font)

	// Border
	style.setBorderTop(BorderStyle.THIN)
	style.setBorderBottom(BorderStyle.THIN)
	style.setBorderLeft(BorderStyle.THIN)
	style.setBorderRight(BorderStyle.THIN)
	style.topBorderColor = IndexedColors.BLACK.getIndex()
	style.bottomBorderColor = IndexedColors.BLACK.getIndex()
	style.leftBorderColor = IndexedColors.BLACK.getIndex()
	style.rightBorderColor = IndexedColors.BLACK.getIndex()

	// Alignment
	style.setVerticalAlignment(VerticalAlignment.CENTER)
	style.setAlignment(HorizontalAlignment.CENTER)
	style.wrapText = true

	return style
}

private fun createHeaderStyle(wb: XSSFWorkbook): XSSFCellStyle {
	val style = wb.createCellStyle()

	// Font
	val dateFont = wb.createFont()
	dateFont.fontHeightInPoints = 11.toShort()
	dateFont.color = IndexedColors.WHITE.getIndex()
	style.setFont(dateFont)

	// Border
	style.setBorderTop(BorderStyle.THIN)
	style.setBorderBottom(BorderStyle.THIN)
	style.setBorderLeft(BorderStyle.THIN)
	style.setBorderRight(BorderStyle.THIN)
	style.topBorderColor = IndexedColors.BLACK.getIndex()
	style.bottomBorderColor = IndexedColors.BLACK.getIndex()
	style.leftBorderColor = IndexedColors.BLACK.getIndex()
	style.rightBorderColor = IndexedColors.BLACK.getIndex()

	// Alignment
	style.setAlignment(HorizontalAlignment.CENTER)
	style.setVerticalAlignment(VerticalAlignment.CENTER)
	style.fillForegroundColor = IndexedColors.GREY_50_PERCENT.getIndex()
	style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
	style.wrapText = true

	return style
}

private fun createMonthNameStyle(wb: XSSFWorkbook): XSSFCellStyle {
	val style = wb.createCellStyle()

	// Font
	val titleFont = wb.createFont()
	titleFont.fontHeightInPoints = 18.toShort()
	titleFont.bold = true
	style.setFont(titleFont)

	// Alignment
	style.setAlignment(HorizontalAlignment.CENTER)
	style.setVerticalAlignment(VerticalAlignment.CENTER)

	return style
}