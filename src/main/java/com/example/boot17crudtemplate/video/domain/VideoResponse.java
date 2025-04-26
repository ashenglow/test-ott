package com.example.boot17crudtemplate.video.domain;

import lombok.Data;

@Data
public class VideoResponse {
    private String id;
    private String title;
    private String videoId;
    private String thumbnail;
    private String description;
}
