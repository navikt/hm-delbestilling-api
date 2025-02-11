package no.nav.hjelpemidler.delbestilling.hjelpemidler.dataV2

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.Navn
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory

class Xlsx {

    private val HEADER_ROW = 2
    private val CONTENT_FIRST_ROW = 4

    fun parse(): XlsxData {
        val inputStream = this::class.java.getResourceAsStream("/cross_og_netti.xlsx")
        val workbook = WorkbookFactory.create(inputStream)

        val sheet = workbook.getSheetAt(0)
        val headers = sheet.getRow(HEADER_ROW).mapIndexed { index, cell -> (index to cell.stringCellValue) }.toMap()

        val deler = mutableMapOf<Hmsnr, Navn>()
        val hjelpemidler = mutableMapOf<Hmsnr, Navn>()
        val hjmTilDeler = mutableMapOf<Hmsnr, MutableSet<Hmsnr>>()

        val dataFormatter = DataFormatter()
        for (rowIdx in CONTENT_FIRST_ROW until sheet.lastRowNum) {
            val row = sheet.getRow(rowIdx) ?: break

            val hjmCell = row.getCell(0)?.stringCellValue
            if (hjmCell.isNullOrBlank()) {
                break
            }

            val hjmHmsnr = hjmCell.takeLast(8).removeSurrounding("(", ")")
            val hjmNavn = hjmCell.dropLast(8).trim()
            hjelpemidler[hjmHmsnr] = hjmNavn

            for (colIdx in 1 until row.lastCellNum) {
                val delHmsnr = dataFormatter.formatCellValue(row.getCell(colIdx)).trim()
                if (delHmsnr.isBlank()) continue
                val delNavn = headers[colIdx] ?: error("Mangler delnavn (header) for $delHmsnr (kolonne: $colIdx)")
                deler.putIfAbsent(delHmsnr, delNavn)

                hjmTilDeler.putIfAbsent(hjmHmsnr, mutableSetOf())
                hjmTilDeler[hjmHmsnr]!!.add(delHmsnr)
            }
        }

        return XlsxData(hjelpemidler = hjelpemidler, deler = deler, hjmTilDeler = hjmTilDeler)
    }
}

data class XlsxData(
    val hjelpemidler: Map<Hmsnr, Navn>,
    val deler: Map<Hmsnr, Navn>,
    val hjmTilDeler: Map<Hmsnr, Set<Hmsnr>>,
)