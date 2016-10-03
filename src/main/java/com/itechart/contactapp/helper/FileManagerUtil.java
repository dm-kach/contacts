package com.itechart.contactapp.helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class FileManagerUtil implements FileManager {

    private static final Logger log = LogManager.getLogger(FileManagerUtil.class);
    private Properties properties;

    public FileManagerUtil(Properties theProperties) {
        properties = theProperties;
    }

    @Override
    public String[] uploadAttachment(HttpServletRequest request, HttpServletResponse response, int dir) {
        String[] fileName = null;
        try {
            List<Part> fileParts = request.getParts().stream().filter(part -> "attachment".equals(part.getName())).collect(Collectors.toList());

            int i = 0;
            fileName = new String[fileParts.size()];
            log.debug("FILES COUNT {}", fileParts.size());
            for (Part filePart : fileParts) {
                fileName[i] = filePart.getSubmittedFileName();
                InputStream fileContent = filePart.getInputStream();
                if (StringUtils.isEmpty(fileName[i]))
                    return null;
                log.debug("UPLOAD LOCATION = {}, FILE NAME = {}", properties.getProperty("users.attachments"), fileName);
                File repository = new File(properties.getProperty("users.attachments") + "/" + dir);
                if (!repository.exists()) {
                    repository.mkdirs();
                }
                File newAttachment = new File(repository, fileName[i]);
                Files.copy(fileContent, newAttachment.toPath(), StandardCopyOption.REPLACE_EXISTING);
                i++;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return fileName;
    }

    @Override
    public void removeAttachment(int dir, String fileName) {
        String path = properties.getProperty("users.attachments") + "/" + dir + "/" + fileName;
        log.debug("Removing file {}", path);
        File deleteFile = new File(path);
        if (deleteFile.exists())
            deleteFile.delete();
    }

    @Override
    public void removeAllAttachments(int dir) {
        String path = properties.getProperty("users.attachments") + "/" + dir;
        File deleteFile = new File(path);
        if (deleteFile.exists()) {
            log.debug("Removing file {}", path);
            FileUtils.deleteQuietly(deleteFile);
        }
    }

    @Override
    public String uploadProfilePhoto(HttpServletRequest request, HttpServletResponse response, String oldPhoto) {
        String fileName = null;

        try {
            Part filePart = request.getPart("profilePhoto");
            fileName = filePart.getSubmittedFileName();
            if (StringUtils.isEmpty(fileName))
                return oldPhoto;
            InputStream fileContentStream = filePart.getInputStream();
            File repository = new File(properties.getProperty("users.photo"));
            if (!repository.exists()) {
                repository.mkdirs();
            }
            log.debug("Photo filename = {}", fileName);

            String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
            File newPhoto = File.createTempFile("usr", fileExtension, repository);
            Files.copy(fileContentStream, newPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fileName = newPhoto.getName();

            if (oldPhoto != null) {
                String path = properties.getProperty("users.photo") + "/" + oldPhoto;
                log.debug("Removing old file {}", path);
                File deleteFile = new File(path);
                if (deleteFile.exists())
                    deleteFile.delete();
            }
        } catch (Exception e) {
            log.error(e);
        }
        return fileName;
    }
}
