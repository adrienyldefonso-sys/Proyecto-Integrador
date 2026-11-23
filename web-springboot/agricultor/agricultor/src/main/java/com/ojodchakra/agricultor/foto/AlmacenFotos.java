package com.ojodchakra.agricultor.foto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AlmacenFotos {

    private static final Logger LOG = LoggerFactory.getLogger(AlmacenFotos.class);

    private final Path raiz;

    public AlmacenFotos(@Value("${app.media-dir}") String carpeta) {
        this.raiz = Path.of(carpeta).toAbsolutePath().normalize();
        LOG.info("Las fotos se guardan en: {}", raiz);
    }

    /** Ruta relativa que Django espera en su campo ImageField. */
    public static String rutaRelativa(String id, String extension) {
        return "diagnosticos/" + id + "." + extension;
    }

    public void guardar(String rutaRelativa, byte[] contenido) {
        Path destino = raiz.resolve(rutaRelativa).normalize();
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException("La ruta de la foto sale de la carpeta media.");
        }
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la foto", e);
        }
    }

    public boolean existe(String rutaRelativa) {
        return Files.exists(raiz.resolve(rutaRelativa).normalize());
    }
}