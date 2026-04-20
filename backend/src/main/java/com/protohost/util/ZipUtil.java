package com.protohost.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipUtil {

    public record ExtractResult(String entryFile, List<String> files) {}

    public ExtractResult extract(InputStream zipStream, File destDir) throws IOException {
        destDir.mkdirs();
        String destCanonical = destDir.getCanonicalPath();
        List<String> htmlFiles = new ArrayList<>();

        // Read all bytes first so we can retry with different charset if needed
        byte[] zipBytes = zipStream.readAllBytes();

        // Try UTF-8 first, fall back to GBK
        Charset charset = StandardCharsets.UTF_8;
        try (ZipInputStream probe = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry e = probe.getNextEntry();
            if (e != null) {
                // Check if name contains replacement char (U+FFFD) indicating bad UTF-8 decode
                if (e.getName().contains("\uFFFD")) {
                    charset = Charset.forName("GBK");
                }
            }
        } catch (Exception ignored) {
            charset = Charset.forName("GBK");
        }

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory() || shouldIgnore(entry.getName())) {
                    zis.closeEntry();
                    continue;
                }
                String safePath = toSafePath(entry.getName());
                File target = new File(destDir, safePath);
                if (!target.getCanonicalPath().startsWith(destCanonical + File.separator)) {
                    throw new SecurityException("Path traversal detected: " + entry.getName());
                }
                target.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(target)) {
                    zis.transferTo(fos);
                }
                if (safePath.endsWith(".html") || safePath.endsWith(".htm")) {
                    htmlFiles.add(safePath);
                }
                zis.closeEntry();
            }
        }

        String entryFile = htmlFiles.stream()
                .filter(f -> f.equals("index.html") || f.endsWith("/index.html"))
                .findFirst()
                .orElse(htmlFiles.isEmpty() ? "index.html" : htmlFiles.get(0));

        return new ExtractResult(entryFile, htmlFiles);
    }

    private boolean shouldIgnore(String path) {
        String name = Paths.get(path).getFileName().toString();
        return path.contains("__MACOSX") || name.equals(".DS_Store")
                || name.equals("Thumbs.db") || name.startsWith("._");
    }

    private String toSafePath(String path) {
        return path.replaceAll("[\\\\:*?\"<>|]", "_");
    }
}
