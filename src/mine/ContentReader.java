/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mine;

import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.htmlparser.parserapplications.*;
import org.htmlparser.util.ParserException;
import java.io.*;
import java.util.*;
/**
 *
 * @author wella
 */
public class ContentReader {
    //reads *.doc and *.docx
    public String readDocFile(String filename) {
        String content = "";
        File file = new File(filename);
        try {
            FileInputStream stream = new FileInputStream(file);
            if(filename.endsWith(".doc")) {
                POIFSFileSystem fs = new POIFSFileSystem(stream);
                HWPFDocument doc = new HWPFDocument(fs);
                WordExtractor we = new WordExtractor(doc);
                String[] paragraphs = we.getParagraphText();
                for( int i=0; i<paragraphs.length; i++ ) {
                    content += paragraphs[i];
                }
            }
            else if(filename.endsWith(".docx")) {
                XWPFDocument doc = new XWPFDocument(stream);
                XWPFWordExtractor we = new XWPFWordExtractor(doc);
                String text = we.getText();
                content += text;
            }
        }
        catch(Exception e) {e.printStackTrace(); }
        return content;
    }

    //reads *.pdf files
    public String readPDFFile(String filename) {
        String content = "";
        try {
            File file = new File(filename);
            PDDocument pdd = new PDDocument();
            pdd = PDDocument.load(file);
            PDFTextStripper pts = new PDFTextStripper();
            content += pts.getText(pdd);
            pdd.close();
        }
        catch(Exception e) { e.printStackTrace(); }
        return content;
    }

    //reads *.ppt and *.pptx
    public String readPPTFile(String filename) {
        String content = "";
        File file = new File(filename);
        POIFSFileSystem fs = null;
        try {
            FileInputStream stream = new FileInputStream(file);
            if(filename.endsWith(".ppt")) {
                fs = new POIFSFileSystem(stream);
                HSLFSlideShow ppt = new HSLFSlideShow(fs);
                PowerPointExtractor powerPointExtractor = new PowerPointExtractor(ppt);
                content += powerPointExtractor.getText(true, true);
            }
            else if(filename.endsWith(".pptx")) {
                XSLFSlideShow ppt = new XSLFSlideShow(filename);
                XSLFPowerPointExtractor powerPointExtractor = new XSLFPowerPointExtractor(ppt);
                content += powerPointExtractor.getText(true, true);
            }
        }
        catch(Exception e) { e.printStackTrace(); }
        return content;
     }

    //reads *.xls and *.xlsx files
     public String readExcelFile(String filename) {
        String content = "";
        if(filename.endsWith(".xls")) {
            content += this.readExcel2003(filename);
        }
        else if(filename.endsWith(".xlsx")) {
            content += this.readExcel2007(filename);
        }
        return content;
     }

     public String readExcel2007(String s) {
        String content = "";
        try {
            Workbook wb = new XSSFWorkbook(s);
            for(int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(0);
                Iterator rows = sheet.iterator();
                while(rows.hasNext()) {
                    XSSFRow row = (XSSFRow)rows.next();
                    Iterator cells = row.cellIterator();
                    while(cells.hasNext()) {
                        XSSFCell cell = (XSSFCell)cells.next();
                        switch(cell.getCellType()) {
                            case XSSFCell.CELL_TYPE_NUMERIC:
                                content += cell.getNumericCellValue();
                                break;
                            case XSSFCell.CELL_TYPE_STRING:
                                String tmp = cell.getRichStringCellValue().getString();
                                content += tmp;
                                break;
                            default: System.out.println("Type not supported"); break;
                        }
                    }
                }
            }

        }
        catch(Exception e) { e.printStackTrace(); }
        return content;
    }

     public String readExcel2003(String s) {
         String content = "";
         try {
            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(s));
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator rows = sheet.rowIterator();
            while(rows.hasNext()) {
                HSSFRow row = (HSSFRow)rows.next();
                Iterator cells = row.cellIterator();
                while(cells.hasNext()) {
                    HSSFCell cell = (HSSFCell)cells.next();
                    switch(cell.getCellType()) {
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            content += cell.getNumericCellValue();
                            break;
                        case HSSFCell.CELL_TYPE_STRING:
                            String tmp = cell.getRichStringCellValue().getString();
                            content += tmp;
                            break;
                        default: System.out.println("Type not supported"); break;
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("!! Bang !! xlRead() : " + e);
        }
         return content;
     }

     //Extract plaintext strings from a web page
     public String readWebText(String source) {
        String text = "";
        StringExtractor sExt = new StringExtractor(source);
        try {
            text = sExt.extractStrings(false);
        }
        catch(ParserException p) {
            p.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return text;
    }
}
