import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownWriter {
    public void guardar(Path destino, String contenido) throws IOException {
        Path absoluto = destino.toAbsolutePath();
        Path directorio = absoluto.getParent();

        if (directorio != null) {
            Files.createDirectories(directorio);
        }

        Files.writeString(absoluto, contenido, StandardCharsets.UTF_8);
    }
}
