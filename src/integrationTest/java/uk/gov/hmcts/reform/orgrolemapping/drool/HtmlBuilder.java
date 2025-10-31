package uk.gov.hmcts.reform.orgrolemapping.drool;

import java.util.List;

public class HtmlBuilder {

    private static final String BODY = "~BODY~";
    private static final String HEADING1 = "~HEADING1~";
    private static final String TITLE = "~TITLE";

    private static String HTML_PAGE_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>~TITLE~</title>
        </head>
        <body>
            <h1>~HEADING1~</h1>
            ~BODY~
        </body>
        </html>
        """;

    private static String HTML_TABLE_TEMPLATE = """
        <table border="1">
            <tr>
            ~TABLE_HEADERS~
            </tr>
            ~TABLE_ROWS~
        </table>
        """;

    public static String buildHtmlPage(String title, String heading1, String body) {
        return HTML_PAGE_TEMPLATE.replace(TITLE, title)
                .replace(HEADING1, heading1)
                .replace(BODY, body);
    }

    public static String buildHtmlTable(List<String> tableHeaders, List<List<String>> tableRows) {
        return HTML_TABLE_TEMPLATE
                .replace("~TABLE_HEADERS~", buildTableHeaders(tableHeaders))
                .replace("~TABLE_ROWS~", buildTableRows(tableRows));
    }

    public static String buildHyperlink(String url, String linkText) {
        return String.format("<a href=\"%s\">%s</a>", url, linkText);
    }

    public static String buildParagraph(String url, String text) {
        return String.format("<p>%s</p>", text);
    }

    public static String buildHeading2(String text) {
        return String.format("<h2>%s</h2>", text);
    }

    public static String buildLine(String text) {
        return String.format("<li>%s</li>", text);
    }

    private static String buildTableHeaders(List<String> tableHeaders) {
        return tableHeaders.size() == 0 ? "" :
                String.format("<tr>%s</tr>",
                String.join("", tableHeaders.stream()
                        .map(header -> "<th>" + header + "</th>")
                        .toList()));
    }

    private static String buildTableRows(List<List<String>> tableRows) {
        return tableRows.size() == 0 ? "" :
                String.join("", tableRows.stream()
                        .map(row -> "<tr>"
                                + String.join("", tableRows.stream()
                                        .map(cell -> "<td>" + cell + "</td>")
                                        .toList())
                                + "</tr>")
                        .toList());
    }
}
