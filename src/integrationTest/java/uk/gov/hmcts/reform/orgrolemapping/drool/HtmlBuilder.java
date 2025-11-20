package uk.gov.hmcts.reform.orgrolemapping.drool;

public class HtmlBuilder {

    public static final String COLLAPSE_HEADER_STYLE_CLASS = "collapsible";
    public static final String COLLAPSE_CONTENT_STYLE_CLASS = "content";
    public static final String COLLAPSE_STYLE = """
            .collapsible {
              cursor: pointer;
              padding: 18px;
              width: 100%;
              border: none;
              text-align: left;
              outline: none;
            }
            .collapsible:after {
              content: '\\02795'; /* Unicode character for "plus" sign (+) */
              font-size: 13px;
              float: right;
              margin-left: 5px;
            }
            .collapsibleActive, .collapsible:hover {}
            .collapsibleActive:after {
              content: "\\2796"; /* Unicode character for "minus" sign (-) */
            }
            .content {
              padding: 0 18px;
              display: none;
              overflow: hidden;
            }
            """;
    public static final String COLLAPSE_SCRIPT = """
            var coll = document.getElementsByClassName("collapsible");
            var i;
            
            for (i = 0; i < coll.length; i++) {
              coll[i].addEventListener("click", function() {
                this.classList.toggle("collapsibleActive");
                var content = this.nextElementSibling;
                if (content.style.display === "block") {
                  content.style.display = "none";
                } else {
                  content.style.display = "block";
                }
              });
            }
            """;

    private static final String BODY = "~BODY~";
    private static final String HEADING1 = "~HEADING1~";
    private static final String SCRIPT = "~SCRIPT~";
    private static final String STYLE = "~STYLE~";
    private static final String TITLE = "~TITLE";

    public static String buildHtmlPage(String title, String style, String heading1,
                                       String body, String script) {
        final String htmlPageTemplate = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>~STYLE~</style>
                <title>~TITLE~</title>
            </head>
            <body>
                <h1>~HEADING1~</h1>
                ~BODY~
                <script>~SCRIPT~</script>
            </body>
            </html>
            """;
        return htmlPageTemplate.replace(TITLE, title)
                .replace(STYLE, style)
                .replace(HEADING1, heading1)
                .replace(BODY, body)
                .replace(SCRIPT, script);
    }

    public static String buildHyperlink(String url, String linkText) {
        return String.format("<a href=\"%s\">%s</a>", url, linkText);
    }

    public static String buildHeading2(String text) {
        return String.format("<h2>%s</h2>", text);
    }

    public static String buildLine(String text) {
        return String.format("<li>%s</li>", text);
    }

    public static String buildButton(String styleClass, String text) {
        return String.format("<button type=\"button\"%s>%s</button>",
                buildStyleClassAttribute(styleClass), text);
    }

    private static String buildStyleClassAttribute(String styleClass) {
        return (styleClass == null || styleClass.isEmpty()) ? "" :
                String.format(" class=\"%s\"", styleClass);
    }

    public static String buildDiv(String styleClass, String text) {
        return String.format("<div%s>%s</div>", buildStyleClassAttribute(styleClass), text);
    }
}
