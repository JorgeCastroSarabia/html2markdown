import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarkdownConverter {
    public String convertir(HtmlParser.HtmlNode document, String sourceUrl) {
        StringBuilder result = new StringBuilder();
        URI base = URI.create(sourceUrl);
        for (HtmlParser.HtmlNode child : document.children) {
            result.append(render(child, base, false));
        }
        return limpiarBloques(result.toString());
    }

    private String render(HtmlParser.HtmlNode node, URI base, boolean inPre) {
        if (node.isText()) {
            return inPre ? node.text : escaparTexto(node.text);
        }

        String tag = node.tag.toLowerCase(Locale.ROOT);
        if (tag.equals("script") || tag.equals("style") || tag.equals("noscript")
                || tag.equals("head") || tag.equals("title") || tag.equals("meta")
                || tag.equals("link")) {
            return "";
        }

        if (tag.matches("h[1-6]")) {
            int level = Character.digit(tag.charAt(1), 10);
            return "\n\n" + "#".repeat(level) + " " +
                    renderChildren(node, base, false).trim() + "\n\n";
        }
        if (tag.equals("p") || tag.equals("div") || tag.equals("section")
                || tag.equals("article") || tag.equals("header") || tag.equals("footer")
                || tag.equals("main") || tag.equals("nav")) {
            return "\n\n" + renderChildren(node, base, false).trim() + "\n\n";
        }
        if (tag.equals("strong") || tag.equals("b")) {
            return "**" + renderChildren(node, base, inPre).trim() + "**";
        }
        if (tag.equals("em") || tag.equals("i")) {
            return "*" + renderChildren(node, base, inPre).trim() + "*";
        }
        if (tag.equals("a")) {
            String href = node.attributes.get("href");
            String text = renderChildren(node, base, inPre).trim();
            return href == null || href.isBlank() ? text :
                    "[" + text + "](" + resolver(base, href) + ")";
        }
        if (tag.equals("img")) {
            String alt = node.attributes.getOrDefault("alt", "");
            String src = node.attributes.get("src");
            return src == null || src.isBlank() ? "" :
                    "![" + alt + "](" + resolver(base, src) + ")";
        }
        if (tag.equals("br")) return "  \n";
        if (tag.equals("hr")) return "\n\n---\n\n";
        if (tag.equals("code")) {
            return "`" + renderChildren(node, base, true).replace("`", "\\`") + "`";
        }
        if (tag.equals("pre")) {
            String code = renderChildren(node, base, true).replaceAll("\\n+$", "");
            return "\n\n```\n" + code + "\n```\n\n";
        }
        if (tag.equals("blockquote")) {
            String content = renderChildren(node, base, false).trim();
            return "\n\n" + content.lines()
                    .map(line -> line.isBlank() ? ">" : "> " + line)
                    .reduce((a, b) -> a + "\n" + b).orElse("") + "\n\n";
        }
        if (tag.equals("ul") || tag.equals("ol")) {
            return "\n\n" + renderList(node, base, 0, tag.equals("ol")) + "\n\n";
        }
        if (tag.equals("li")) {
            return renderChildren(node, base, inPre);
        }
        return renderChildren(node, base, inPre);
    }

    private String renderList(HtmlParser.HtmlNode list, URI base,
                              int depth, boolean ordered) {
        StringBuilder out = new StringBuilder();
        int number = 1;
        for (HtmlParser.HtmlNode child : list.children) {
            if (child.isText()) continue;
            if (!child.tag.equals("li")) {
                out.append(render(child, base, false));
                continue;
            }

            String indent = "  ".repeat(depth);
            String marker = ordered ? (number++) + ". " : "- ";
            StringBuilder itemText = new StringBuilder();
            for (HtmlParser.HtmlNode itemChild : child.children) {
                if (itemChild.tag.equals("ul") || itemChild.tag.equals("ol")) {
                    continue;
                }
                itemText.append(render(itemChild, base, false));
            }
            String text = itemText.toString().replaceAll("\\s+", " ").trim();
            out.append(indent).append(marker).append(text).append("\n");

            for (HtmlParser.HtmlNode itemChild : child.children) {
                if (itemChild.tag.equals("ul") || itemChild.tag.equals("ol")) {
                    out.append(renderList(itemChild, base, depth + 1,
                            itemChild.tag.equals("ol")));
                }
            }
        }
        return out.toString().replaceAll("\\n+$", "");
    }

    private String renderChildren(HtmlParser.HtmlNode node, URI base, boolean inPre) {
        StringBuilder out = new StringBuilder();
        for (HtmlParser.HtmlNode child : node.children) {
            out.append(render(child, base, inPre));
        }
        return out.toString();
    }

    private String resolver(URI base, String reference) {
        try {
            return base.resolve(new URI(reference)).toString()
                    .replace(" ", "%20");
        } catch (IllegalArgumentException | URISyntaxException e) {
            return reference.replace(" ", "%20");
        }
    }

    private String escaparTexto(String text) {
        return text.replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("[", "\\[")
                .replace("]", "\\]");
    }

    private String limpiarBloques(String text) {
        return text.replace("\r", "")
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n[ \\t]+", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim() + "\n";
    }
}
