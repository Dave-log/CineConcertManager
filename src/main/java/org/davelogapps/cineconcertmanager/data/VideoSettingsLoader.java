package org.davelogapps.cineconcertmanager.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VideoSettingsLoader {
    public void loadVideoSettings(List<VideoFile> videoFiles) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("videoData.json");

            if (inputStream != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(inputStream);

                for (JsonNode node : rootNode) {
                    String filename = node.get("filename").asText();
                    boolean mute = node.get("mute").asBoolean();

                    for (VideoFile videoFile : videoFiles) {
                        if (videoFile.getFilename().equals(filename)) {
                            videoFile.setMute(mute);
                            break;
                        }
                    }
                }
            } else {
                System.out.println("JSON file not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
