package com.example.boot17crudtemplate.video.api;

import com.example.boot17crudtemplate.video.domain.VideoResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@RestController
public class VideoController {
    private final ObjectMapper objectMapper =  new ObjectMapper();
    @GetMapping("/api/video/lower")
    public ResponseEntity<String> lower() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/LowerCasePythonRunner.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            return new ResponseEntity<>(output.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crawl")
    public ResponseEntity<String> crawlYoutubeData() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/Youtube_fetcher.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor(); // 크롤링 끝날 때까지 기다리기

            if (exitCode == 0) {
                return ResponseEntity.ok("Crawling completed successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Crawling failed. Output: " + output.toString());
            }

        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // 2. 크롤링된 데이터 가져오는 엔드포인트 (프론트엔드용)
    @GetMapping("/api/videos")
    public ResponseEntity<List<VideoResponse>> getYoutubeVideos() {
        try {
            File jsonFile = new File("scripts/updated_movies.json");
            List<VideoResponse> videos = objectMapper.readValue(
                    jsonFile,
                    new TypeReference<List<VideoResponse>>() {}
            );
            return ResponseEntity.ok(videos);

        } catch (IOException e) {
            // 만약 파일을 못 읽으면 500 에러
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
