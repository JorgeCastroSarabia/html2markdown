import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class HtmlParser {
    public static class HtmlNode {
        public final String tag;
        public final String text;
        public final Map<String, String> attributes = new LinkedHashMap<>();
        public final List<HtmlNode> children = new ArrayList<>();

        public HtmlNode(String tag, String text) {
            this.tag = tag;
            this.text = text;
        }

        public boolean isText() {
            return "#text".equals(tag);
        }
    }

    public HtmlNode parse(String html) throws IOException {
        HtmlNode root = new HtmlNode("document", "");
        List<HtmlNode> stack = new ArrayList<>();
        stack.add(root);

        new ParserDelegator().parse(new StringReader(html),
                new HTMLEditorKit.ParserCallback() {
                    @Override
                    public void handleStartTag(HTML.Tag tag,
                                               MutableAttributeSet attributes,
                                               int position) {
                        HtmlNode node = new HtmlNode(tag.toString().toLowerCase(), "");
                        copyAttributes(attributes, node);
                        stack.get(stack.size() - 1).children.add(node);
                        stack.add(node);
                    }

                    @Override
                    public void handleEndTag(HTML.Tag tag, int position) {
                        String closing = tag.toString().toLowerCase();
                        for (int i = stack.size() - 1; i > 0; i--) {
                            if (stack.get(i).tag.equals(closing)) {
                                while (stack.size() > i) {
                                    stack.remove(stack.size() - 1);
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void handleSimpleTag(HTML.Tag tag,
                                                MutableAttributeSet attributes,
                                                int position) {
                        HtmlNode node = new HtmlNode(tag.toString().toLowerCase(), "");
                        copyAttributes(attributes, node);
                        stack.get(stack.size() - 1).children.add(node);
                    }

                    @Override
                    public void handleText(char[] data, int position) {
                        stack.get(stack.size() - 1).children.add(
                                new HtmlNode("#text", new String(data)));
                    }
                }, true);

        return root;
    }

    private void copyAttributes(MutableAttributeSet source, HtmlNode target) {
        var names = source.getAttributeNames();
        while (names.hasMoreElements()) {
            Object key = names.nextElement();
            Object value = source.getAttribute(key);
            if (key != null && value != null) {
                target.attributes.put(key.toString().toLowerCase(), value.toString());
            }
        }
    }
}
