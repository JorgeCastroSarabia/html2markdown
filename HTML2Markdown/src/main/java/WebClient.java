import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;


public class WebClient {
    private static final int MAX_BYTES = 2 * 1024 * 1024;

    public String descargarHTML(URL url) throws IOException {
        String protocol = url.getProtocol().toLowerCase(Locale.ROOT);
        if (!protocol.equals("http") && !protocol.equals("https")) {
            throw new IOException("Solo se permiten URL HTTP o HTTPS.");
        }

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "HTML2Markdown/1.0");

        if (!(connection instanceof HttpURLConnection)) {
            throw new IOException("No fue posible crear una conexión HTTP.");
        }

        HttpURLConnection http = (HttpURLConnection) connection;
        http.setRequestMethod("GET");
        http.setInstanceFollowRedirects(true);

        try {
            int status = http.getResponseCode();
            System.out.println("Respuesta HTTP: " + status);
            if (status < 200 || status >= 300) {
                throw new IOException("El servidor respondió con HTTP " + status + ".");
            }

            String contentType = http.getContentType();
            if (contentType == null ||
                    !contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
                throw new IOException("El recurso no tiene tipo de contenido text/html.");
            }

            Charset charset = detectarCharset(contentType);
            try (InputStream in = http.getInputStream();
                 ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                int total = 0;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_BYTES) {
                        throw new IOException("El HTML supera el límite de 2 MB.");
                    }
                    bytes.write(buffer, 0, read);
                }
                return bytes.toString(charset);
            }
        } finally {
            http.disconnect();
        }
    }

    private Charset detectarCharset(String contentType) {
        String lower = contentType.toLowerCase(Locale.ROOT);
        int index = lower.indexOf("charset=");
        if (index >= 0) {
            String name = contentType.substring(index + 8).trim();
            int semicolon = name.indexOf(';');
            if (semicolon >= 0) name = name.substring(0, semicolon).trim();
            name = name.replace("\"", "").replace("'", "");
            try {
                return Charset.forName(name);
            } catch (Exception ignored) {
                // Se utiliza UTF-8 si el nombre no es válido.
            }
        }
        return StandardCharsets.UTF_8;
    }
}
