# HTML2Markdown

Aplicación de consola desarrollada en Java 17 o superior que descarga una página HTML y convierte
un subconjunto de su contenido a Markdown. Utiliza exclusivamente la API estándar de Java.

## Requisitos
- JDK 17 o superior.
- IntelliJ IDEA (opcional).
- Conexión a Internet para convertir páginas remotas.

## Estructura
```text
HTML2Markdown/
├── README.md
├── .gitignore
├── src/main/java/
│   ├── HTML2Markdown.java
│   ├── WebClient.java
│   ├── HtmlParser.java
│   ├── MarkdownConverter.java
│   └── MarkdownWriter.java
├── tests/
│   ├── encabezados.html
│   ├── listas.html
│   ├── enlaces.html
│   └── documento_completo.html
└── output/
```

## Compilar y ejecutar

Desde la carpeta raíz del proyecto:

```bash
javac -encoding UTF-8 -d out src/main/java/*.java
java -cp out HTML2Markdown https://ejemplo.org/articulo.html
```

Para indicar un archivo de salida:

```bash
java -cp out HTML2Markdown https://ejemplo.org/articulo.html output/articulo.md
```

También puedes abrir la carpeta en IntelliJ IDEA, marcar `src/main/java` como Sources Root,
seleccionar un JDK 17 y ejecutar `HTML2Markdown` pasando la URL como argumento del programa.

## Componentes
- **HTML2Markdown:** valida argumentos y coordina el proceso.
- **WebClient:** descarga HTML con `URL`, `URLConnection` y `HttpURLConnection`.
- **HtmlParser:** analiza el documento con `ParserDelegator` y construye un árbol de nodos.
- **MarkdownConverter:** transforma los nodos a Markdown.
- **MarkdownWriter:** crea directorios y guarda el resultado en UTF-8.

## Alcance y limitaciones
Se convierten encabezados, párrafos, negritas, cursivas, enlaces, imágenes, listas, saltos de línea,
citas, código en línea, bloques de código y separadores. Se omiten scripts, estilos y contenido de
cabecera no pertinente. Las URL relativas se resuelven respecto a la URL de origen.

`ParserDelegator` es un analizador heredado y no interpreta perfectamente todos los documentos HTML5.
La conversión trabaja con un subconjunto de HTML; no es un navegador. No ejecuta JavaScript,
no descarga imágenes y no sigue enlaces a otras páginas. El tamaño máximo de HTML es 2 MB.

## Preguntas de reflexión

1. **¿Qué papel desempeña la aplicación dentro de una arquitectura cliente-servidor?**  
   La aplicación funciona como cliente. Envía una solicitud HTTP a un servidor web y recibe el
   documento HTML para procesarlo localmente.

2. **¿Qué responsabilidades corresponden al servidor y cuáles al cliente?**  
   El servidor recibe la solicitud y responde con el recurso solicitado y sus datos HTTP. El cliente
   establece la conexión, valida la respuesta, interpreta el HTML, lo transforma y guarda el archivo.

3. **¿Qué diferencia existe entre recuperar HTML e interpretar su estructura?**  
   Recuperar HTML significa descargar el texto enviado por el servidor. Interpretarlo significa
   reconocer las etiquetas, atributos, jerarquía y contenido del documento.

4. **¿Por qué no basta reemplazar etiquetas HTML con expresiones regulares?**  
   Porque HTML puede contener elementos anidados, atributos y estructuras cuya interpretación
   requiere conocer el contexto. Una sustitución simple puede alterar el orden o el contenido.

5. **¿Qué dificultades presentan los elementos anidados?**  
   Se debe conservar el orden y la jerarquía. Por ejemplo, una lista puede contener otra lista y un
   enlace puede incluir texto en negritas.

6. **¿Qué información puede perderse al convertir HTML a Markdown?**  
   Pueden perderse estilos visuales, distribución, tamaños, colores, interacciones y elementos que
   Markdown no representa de la misma manera.

7. **¿Por qué deben resolverse las URL relativas?**  
   Porque una referencia como `imagenes/foto.png` necesita combinarse con la dirección base de la
   página para obtener una URL completa y utilizable.

8. **¿Qué ventajas ofrece separar descarga, análisis y conversión en clases?**  
   Cada clase tiene una responsabilidad concreta. Esto facilita probar, mantener, corregir y ampliar
   el programa sin mezclar toda la lógica.

9. **¿Qué limitaciones presenta ParserDelegator con HTML moderno?**  
   Es un analizador heredado, basado principalmente en HTML 3.2, y no garantiza interpretar
   correctamente todas las características de HTML5.

10. **¿Qué modificaciones serían necesarias para convertir tablas HTML a Markdown?**  
    Se necesitaría reconocer las etiquetas `table`, `tr`, `th` y `td`, guardar las filas y columnas
    respetando su orden y generar la sintaxis de encabezados y separadores de una tabla Markdown.

## Pruebas
Los archivos de `tests/` contienen ejemplos locales para revisar encabezados, listas, enlaces,
formato de texto, imágenes, código, entidades y elementos anidados. La aplicación recibe una URL
HTTP/HTTPS; para probar los documentos HTML locales se puede publicar temporalmente la carpeta
con un servidor HTTP local.

## Restricción de bibliotecas
No se utilizan Jsoup, flexmark, CommonMark, Apache HttpClient ni bibliotecas externas.
