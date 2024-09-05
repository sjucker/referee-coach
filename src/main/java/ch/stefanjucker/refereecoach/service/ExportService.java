package ch.stefanjucker.refereecoach.service;

import static org.springframework.data.domain.Sort.Order.desc;
import static org.springframework.data.domain.Sort.by;

import ch.stefanjucker.refereecoach.domain.repository.VideoReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class ExportService {

    private final VideoReportRepository videoReportRepository;

    public ExportService(VideoReportRepository videoReportRepository) {
        this.videoReportRepository = videoReportRepository;
    }

    public File export() throws IOException {
        try (var wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet();
            wb.setSheetName(0, "Export");

            var creationHelper = wb.getCreationHelper();
            var dateCellStyle = wb.createCellStyle();
            dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd.MM.yyyy"));

            var rowIndex = 0;
            // headers
            var headerRow = sheet.createRow(rowIndex++);

            var columnIndex = 0;
            headerRow.createCell(columnIndex++).setCellValue("Date");
            headerRow.createCell(columnIndex++).setCellValue("Game Number");
            headerRow.createCell(columnIndex++).setCellValue("Competition");
            headerRow.createCell(columnIndex++).setCellValue("Teams");
            headerRow.createCell(columnIndex++).setCellValue("Coach");
            headerRow.createCell(columnIndex++).setCellValue("Referee");
            headerRow.createCell(columnIndex++).setCellValue("Image, Approach");
            headerRow.createCell(columnIndex++).setCellValue("Fitness Condition");
            headerRow.createCell(columnIndex++).setCellValue("Mechanics & Individual Officiating Techniques");
            headerRow.createCell(columnIndex++).setCellValue("Critera: Fouls");
            headerRow.createCell(columnIndex++).setCellValue("Critera: Violations");
            headerRow.createCell(columnIndex++).setCellValue("Game Control and Management");
            headerRow.createCell(columnIndex++).setCellValue("Overall Score");
            headerRow.createCell(columnIndex++).setCellValue("Promotion?");

            for (var videoReport : videoReportRepository.findAll(by(desc("basketplanGame.date"),
                                                                    desc("basketplanGame.gameNumber"),
                                                                    desc("reportee")))) {
                var row = sheet.createRow(rowIndex++);
                columnIndex = 0;

                XSSFCell cell = row.createCell(columnIndex++);
                cell.setCellValue(DateUtil.getExcelDate(videoReport.getBasketplanGame().getDate()));
                cell.setCellStyle(dateCellStyle);

                row.createCell(columnIndex++).setCellValue(videoReport.getBasketplanGame().getGameNumber());
                row.createCell(columnIndex++).setCellValue(videoReport.getBasketplanGame().getCompetition());
                row.createCell(columnIndex++).setCellValue("%s - %s".formatted(videoReport.getBasketplanGame().getTeamA(),
                                                                               videoReport.getBasketplanGame().getTeamB()));
                row.createCell(columnIndex++).setCellValue(videoReport.getCoach().getName());
                row.createCell(columnIndex++).setCellValue(videoReport.relevantReferee().getName());
                if (videoReport.getVersion() > 1) {
                    row.createCell(columnIndex++).setCellValue(videoReport.getImage().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.getFitness().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.getMechanics().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.getFouls().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.getViolations().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.getGameManagement().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.getGeneral().getScore());
                    row.createCell(columnIndex++).setCellValue(videoReport.isPromotion() ? "x" : "");
                }

            }

            for (int i = 0; i < columnIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            Path temp = Files.createTempFile(null, ".xlsx");
            try (var fileOut = new FileOutputStream(temp.toFile())) {
                wb.write(fileOut);
                return temp.toFile();
            }
        }
    }

}
