package io.leanddd.module.file.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RequestMapping("/api/file")
public interface FileService {

    @PostMapping("/upload")
    File uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "path", required = false) String path);

    @GetMapping("/{id}")
    void viewFile(@PathVariable String id, HttpServletResponse response);

    @GetMapping("/{id}/download")
    void downloadFile(@PathVariable String id, HttpServletResponse response);

    @DeleteMapping("/{id}")
    void deleteFile(@PathVariable String id);

}
