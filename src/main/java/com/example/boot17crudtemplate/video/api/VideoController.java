package com.example.boot17crudtemplate.video.api;

import com.example.boot17crudtemplate.video.domain.VideoResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Controller
public class VideoController {
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

    @GetMapping("/api/youtube/list")
    public ResponseEntity<List<VideoResponse>> getYoutubeEmbedData() {
        try {
            // 1. Python 실행 (scripts/Youtube_fetcher.py 실행)
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/Youtube_fetcher.py", "scripts/video_lowercased.json");
            pb.directory(new File(System.getProperty("user.dir"))); // 프로젝트 루트 기준
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor(); // python 끝날 때까지 기다리기

            // 2. updated_movies.json 읽기
            File updatedFile = new File("scripts/updated_movies.json");
            if (!updatedFile.exists()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            List<VideoResponse> videos = objectMapper.readValue(updatedFile, new TypeReference<List<VideoResponse>>() {});

            return new ResponseEntity<>(videos, HttpStatus.OK);

        } catch (Exception e) {
            VideoResponse errorVideo = new VideoResponse();
            errorVideo.setId("-1");
            errorVideo.setTitle("Error");
            errorVideo.setVideoId("");
            errorVideo.setThumbnail("");
            errorVideo.setDescription(e.getMessage());

            List<VideoResponse> errorList = Collections.singletonList(errorVideo);
            return ResponseEntity.ok(errorList);
        }
    }

}
