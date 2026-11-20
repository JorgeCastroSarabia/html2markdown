import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;


public class HTML2Markdown {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            mostrarUso();
            return;
        }

        try {
            URL url = new URL(args[0]);
            String protocol = url.getProtocol();
            if (!protocol.equalsIgnoreCase("http")
                    && !protocol.equalsIgnoreCase("https")) {
                throw new MalformedURLException("La URL debe comenzar con http:// o https://");
            }

            Path destino = args.length == 2
                    ? Path.of(args[1])
                    : Path.of(nombrePredeterminado(url));

            System.out.println("HTML2Markdown");
            System.out.println("--------------------------------");
            System.out.println("URL: " + url);
            System.out.println("Conectando con el servidor...");

            WebClient webClient = new WebClient();
            String html = webClient.descargarHTML(url);

            System.out.println("Analizando HTML...");
            HtmlParser parser = new HtmlParser();
            HtmlParser.HtmlNode document = parser.parse(html);

            System.out.println("Convirtiendo a Markdown...");
            MarkdownConverter converter = new MarkdownConverter();
            String markdown = converter.convertir(document, url.toString());

            MarkdownWriter writer = new MarkdownWriter();
            writer.guardar(destino, markdown);

            long bytes = Files.size(destino);
            System.out.println();
            System.out.println("Archivo generado: " + destino.toAbsolutePath());
            System.out.println("Tamaño: " + bytes + " bytes");
            System.out.println("Conversión finalizada.");

        } catch (MalformedURLException e) {
            System.err.println("URL inválida: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de conexión o archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }
    }

    private static String nombrePredeterminado(URL url) {
        String path = url.getPath();
        String last = path.substring(path.lastIndexOf('/') + 1);
        if (last.isBlank()) last = "pagina";
        int query = last.indexOf('?');
        if (query >= 0) last = last.substring(0, query);
        int fragment = last.indexOf('#');
        if (fragment >= 0) last = last.substring(0, fragment);
        int dot = last.lastIndexOf('.');
        if (dot > 0) last = last.substring(0, dot);
        last = last.replaceAll("[^a-zA-Z0-9._-]", "_");
        return last + ".md";
    }

    private static void mostrarUso() {
        System.out.println("Uso:");
        System.out.println("  java HTML2Markdown https://ejemplo.org/articulo.html");
        System.out.println("  java HTML2Markdown https://ejemplo.org/articulo.html salida.md");
    }
}
