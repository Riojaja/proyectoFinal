package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String FOLDER_NAME = "punamba_uploads";

    public String guardarArchivo(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IOException("El archivo está vacío o no fue enviado.");
        }

        Path rutaDirectorio = Paths.get(FOLDER_NAME).toAbsolutePath().normalize();

        if (!Files.exists(rutaDirectorio)) {
            Files.createDirectories(rutaDirectorio);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            nombreOriginal = "imagen_producto";
        }

        nombreOriginal = nombreOriginal.replace(" ", "_");

        String nombreArchivo = UUID.randomUUID() + "_" + nombreOriginal;

        Path rutaArchivo = rutaDirectorio.resolve(nombreArchivo).normalize();

        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        return Objects.requireNonNull(nombreArchivo, "No se pudo generar el nombre del archivo");
    }
}