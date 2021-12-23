package com.github.aakumykov.simple_audio_recorder;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;

public class MicUtils {

    public static boolean isMicAvailable(Context context) {

        MediaRecorder recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(new File(context.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());

        boolean available = true;

        try {
            recorder.prepare();
            recorder.start();
        }
        catch (Exception exception) {
            available = false;
        }
        finally {
            recorder.release();
        }

        return available;
    }
}
