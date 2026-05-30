package be.ap.backend.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

/**
 * Bouwt een minimalistisch Excel (.xlsx) template in-memory zonder externe
 * libraries.
 *
 * <p>
 * De gegenereerde file is een geldige ZIP-structuur volgens de
 * OOXML-specificatie
 * en bevat een vooraf gedefinieerde worksheet met kolomheaders voor
 * boekmetadata.
 * </p>
 *
 * <p>
 * Dit wordt gebruikt om gebruikers een downloadbaar import-template aan te
 * bieden.
 * </p>
 */
@Component
public class ExcelTemplateBuilder {

    /**
     * Genereert een Excel-template (.xlsx) als byte-array.
     *
     * <p>
     * De output is een volledig opgebouwd ZIP-archief met minimale OOXML-structuur
     * en één worksheet ("Books") met vooraf gedefinieerde kolommen.
     * </p>
     *
     * @return de gegenereerde Excel-file als byte-array
     * @throws IOException indien het schrijven naar de ZIP-stream faalt
     */
    public byte[] buildTemplateXlsx() throws IOException {
        String sheetXml = buildSheetXml();
        String workbookXml = buildWorkbookXml();
        String contentTypes = buildContentTypesXml();
        String rootRels = buildRootRelsXml();
        String workbookRels = buildWorkbookRelsXml();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            writeZipEntry(zos, "[Content_Types].xml", contentTypes);
            writeZipEntry(zos, "_rels/.rels", rootRels);
            writeZipEntry(zos, "xl/workbook.xml", workbookXml);
            writeZipEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels);
            writeZipEntry(zos, "xl/worksheets/sheet1.xml", sheetXml);
        }
        return baos.toByteArray();
    }

    private String buildSheetXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetViews>
                    <sheetView workbookViewId="0" tabSelected="1">
                    <pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/>
                    </sheetView>
                </sheetViews>
                <sheetData>
                    <row r="1">
                    <c r="A1" t="inlineStr"><is><t>ISBN (optioneel)</t></is></c>
                    <c r="B1" t="inlineStr"><is><t>Titel *</t></is></c>
                    <c r="C1" t="inlineStr"><is><t>Auteur *</t></is></c>
                    <c r="D1" t="inlineStr"><is><t>Beschrijving * (max 1000 tekens)</t></is></c>
                    <c r="E1" t="inlineStr"><is><t>Didactisch materiaal (JA/NEE)</t></is></c>
                    <c r="F1" t="inlineStr"><is><t>Uitgever (optioneel)</t></is></c>
                    <c r="G1" t="inlineStr"><is><t>CLIB (optioneel, A/B/C/D)</t></is></c>
                    <c r="H1" t="inlineStr"><is><t>Fictie (JA/NEE)</t></is></c>
                    <c r="I1" t="inlineStr"><is><t>Boektype *</t></is></c>
                    <c r="J1" t="inlineStr"><is><t>Genres * (gescheiden door komma ',')</t></is></c>
                    <c r="K1" t="inlineStr"><is><t>Jaar van uitgave (optioneel)</t></is></c>
                    <c r="L1" t="inlineStr"><is><t>Taal *</t></is></c>
                    <c r="M1" t="inlineStr"><is><t>Aantal pagina's *</t></is></c>
                    <c r="N1" t="inlineStr"><is><t>Thema's (optioneel, gescheiden door komma ',')</t></is></c>
                    <c r="O1" t="inlineStr"><is><t>Lettergrootte (optioneel: groot/medium/klein)</t></is></c>
                    <c r="P1" t="inlineStr"><is><t>Enkel zichtbaar voor deze school (JA/NEE)</t></is></c>
                    <c r="Q1" t="inlineStr"><is><t>Cover (optioneel, URL)</t></is></c>
                    </row>
                </sheetData>
                </worksheet>
                """;
    }

    private String buildWorkbookXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                        xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                <sheets>
                    <sheet name="Books" sheetId="1" r:id="rId1"/>
                </sheets>
                </workbook>
                """;
    }

    private String buildContentTypesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Override PartName="/xl/workbook.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                <Override PartName="/xl/worksheets/sheet1.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """;
    }

    private String buildRootRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    Target="xl/workbook.xml"/>
                </Relationships>
                """;
    }

    private String buildWorkbookRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                    Target="worksheets/sheet1.xml"/>
                </Relationships>
                """;
    }

    private void writeZipEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
