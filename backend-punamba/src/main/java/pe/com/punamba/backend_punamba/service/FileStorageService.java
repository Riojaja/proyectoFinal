package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String FOLDER_NAME = "punamba_uploads";

    public String guardarArchivo(MultipartFile archivo) throws IOException {
        Path rutaDirectorio = Paths.get(FOLDER_NAME).toAbsolutePath().normalize();
        
        if (!Files.exists(rutaDirectorio)) {
            Files.createDirectories(rutaDirectorio);
        }

        String nombreOriginal = archivo.getOriginalFilename() != null 
                ? archivo.getOriginalFilename().replace(" ", "_") 
                : "imagen_producto";

        String nombreArchivo = UUID.randomUUID().toString() + "_" + nombreOriginal;
        
        Path rutaArchivo = rutaDirectorio.resolve(nombreArchivo);

        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }
}