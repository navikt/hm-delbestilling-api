package no.nav.hjelpemidler.delbestilling.hjelpemidler.parsing

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.Navn
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

class Xlsx {

    private val DELER_SHEET = 0
    private val DELER_CONTENT_FIRST_ROW = 1
    private val KOBLINGER_FIRST_SHEET = 1
    private val KOBLINGER_FIRST_ROW = 2
    private val KOBLINGER_FIRST_COL = 2

    fun parse(): XlsxData {
        val inputStream = this::class.java.getResourceAsStream("/deler.xlsx")
        val workbook = WorkbookFactory.create(inputStream)

        val deler = parseDeler(workbook)

        val hjelpemidler = mutableMapOf<Hmsnr, Navn>()
        val hjmTilDeler = mutableMapOf<Hmsnr, MutableSet<Hmsnr>>()
        for (sheetIdx in KOBLINGER_FIRST_SHEET until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(sheetIdx)

            for (rowIdx in KOBLINGER_FIRST_ROW..sheet.lastRowNum) {
                val row = sheet.getRow(rowIdx) ?: break

                val hjmNavn = row.cellString(0)
                val hjmHmsnr = row.cellString(1)
                if (hjmNavn.isNullOrBlank() || hjmHmsnr.isNullOrBlank()) {
                    continue
                }
                require(hjmHmsnr !in hjelpemidler) { "Fant duplikat av hjelpemiddel $hjmHmsnr" }
                hjelpemidler[hjmHmsnr] = hjmNavn

                for (colIdx in KOBLINGER_FIRST_COL until row.lastCellNum) {
                    val delHmsnr = row.cellString(colIdx)
                    if (delHmsnr.isNullOrBlank()) continue

                    require(delHmsnr in deler) { "Mangler detaljer om del $delHmsnr" }

                    hjmTilDeler.putIfAbsent(hjmHmsnr, mutableSetOf())
                    hjmTilDeler[hjmHmsnr]!!.add(delHmsnr)
                }
            }
        }


        return XlsxData(hjelpemidler = hjelpemidler, deler = deler, hjmTilDeler = hjmTilDeler)
    }

    private fun parseDeler(workbook: Workbook): MutableMap<Hmsnr, ParsedDel> {
        val sheetDeler = workbook.getSheetAt(DELER_SHEET)
        val deler = mutableMapOf<Hmsnr, ParsedDel>()
        for (rowIdx in DELER_CONTENT_FIRST_ROW..sheetDeler.lastRowNum) {
            val row = sheetDeler.getRow(rowIdx)
            val navn = row.cellString(0) ?: continue
            val hmsNr = row.cellString(1) ?: error("Del $navn mangler hmsnr")
            val levArtNr = row.cellString(2)
            val defaultAntall = row.cellString(3)
            val maksAntall = row.cellString(4)
            val kategori = row.cellString(5)
            require(hmsNr !in deler) { "Fant duplikat av del med hmsnr $hmsNr" }
            deler[hmsNr] = ParsedDel(
                hmsNr = hmsNr,
                navn = navn,
                levArtNr = levArtNr,
                defaultAntall = defaultAntall,
                maksAntall = maksAntall,
                kategori = kategori,
            )
        }
        return deler
    }
}

data class XlsxData(
    val hjelpemidler: Map<Hmsnr, Navn>,
    val deler: Map<Hmsnr, ParsedDel>,
    val hjmTilDeler: Map<Hmsnr, Set<Hmsnr>>,
)

data class ParsedDel(
    val hmsNr: Hmsnr,
    val navn: Navn,
    val levArtNr: String?,
    val defaultAntall: String?,
    val maksAntall: String?,
    val kategori: String?,
)

val dataFormatter = DataFormatter()
private fun Row.cellString(index: Int): String? {
    val cell = this.getCell(index) ?: return null
    if (cell.cellType == CellType.BLANK) {
        return null
    }
    return dataFormatter.formatCellValue(cell).trim()
}