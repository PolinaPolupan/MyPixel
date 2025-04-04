package com.example.mypixel.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.exception.StorageFileNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.google.common.io.Files.getFileExtension;


@Slf4j
public class TempStorageService implements StorageService {

    private final Path rootLocation;

    public TempStorageService(@NonNull String location) {
        if (location.trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(location);
    }

    @Override
    public void store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            store(inputStream, file.getOriginalFilename());
        } catch (IOException e) {
            throw new StorageException("Failed to access file content.", e);
        }
    }

    @Override
    public void store(Resource file, String filename) {
        log.debug("Storing resource as file: {}", filename);

        try (InputStream inputStream = file.getInputStream()) {
            store(inputStream, filename);
        } catch (IOException e) {
            log.error("Failed to access resource content: {}", e.getMessage(), e);
            throw new StorageException("Failed to access resource content.", e);
        }
    }

    /**
     * Stores a file from an input stream.
     * Note: This method does NOT close the input stream - the caller is responsible for that.
     */
    @Override
    public void store(InputStream inputStream, String filename) {
        log.debug("Storing file with filename: {}", filename);

        if (inputStream == null) {
            throw new StorageException("InputStream cannot be null");
        }

        if (filename == null || filename.isEmpty()) {
            throw new StorageException("Filename cannot be empty");
        }

        try {
            Path destinationFile = resolveAndValidatePath(filename);
            copyToDestination(inputStream, destinationFile, filename);
        } catch (IOException e) {
            log.error("Failed to store file: {}. Error: {}", filename, e.getMessage(), e);
            throw new StorageException("Failed to store file.", e);
        }
    }

    private Path resolveAndValidatePath(String filename) {
        Path destinationFile = this.rootLocation.resolve(
                        Paths.get(filename))
                .normalize().toAbsolutePath();

        log.debug("Destination path resolved to: {}", destinationFile);

        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            // This is a security check against path traversal attacks
            log.error("Security violation: Attempted to store file outside of root location. Path: {}", destinationFile);
            throw new StorageException("Cannot store file outside current directory.");
        }

        return destinationFile;
    }

    private void copyToDestination(InputStream inputStream, Path destinationFile, String filename) throws IOException {
        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully stored file: {} to location: {}", filename, destinationFile);
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("MalformedURLException: Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    private final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");
    private final Pattern prefixPattern = Pattern.compile("^[^_]+_(.*?)(\\.[^.]*$|$)");

    private String addPrefixToFilename(String filename, String prefix) {
        // Remove any existing prefix
        filename = removeExistingPrefix(filename);

        // Add new prefix
        Matcher matcher = filenamePattern.matcher(filename);
        if (matcher.find()) {
            String baseName = matcher.group(1);
            String extension = matcher.group(2);
            return prefix + "_" + baseName + extension;
        }
        return filename;
    }

    @Override
    public String removeExistingPrefix(String filename) {
        Matcher matcher = prefixPattern.matcher(filename);
        if (matcher.find()) {
            String baseName = matcher.group(1);
            String extension = matcher.group(2);
            return baseName + extension;
        }
        return filename;
    }

    @Override
    public String createTempFileFromResource(Resource resource) {
        if (resource != null) {
            String filename = resource.getFilename();
            String extension = getFileExtension(filename);
            String tempName = addPrefixToFilename(filename, UUID.randomUUID().toString());
            store(resource, tempName);

            log.info("Temp file created: [{}], Filename: [{}], Extension: [{}]", tempName, filename, extension);
            return tempName;
        }
        throw new StorageException("Failed to create temp file: Input resource is null");
    }

    @Override
    public boolean fileExists(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
