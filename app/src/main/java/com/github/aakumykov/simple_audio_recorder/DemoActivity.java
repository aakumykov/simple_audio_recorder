package com.github.aakumykov.simple_audio_recorder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.aakumykov.simple_audio_recorder.databinding.ActivityDemoBinding;
import com.github.aakumykov.simple_audio_recorder_module.SimpleAudioRecorder;
import com.github.aakumykov.simple_audio_recorder_module.iSimpleAudioRecorderCallbacks;
import com.gitlab.aakumykov.gapless_audio_player.ErrorCode;
import com.gitlab.aakumykov.gapless_audio_player.GaplessAudioPlayer;
import com.gitlab.aakumykov.gapless_audio_player.SoundItem;
import com.gitlab.aakumykov.gapless_audio_player.iAudioPlayer;

import java.util.Random;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DemoActivity extends AppCompatActivity {

    private static final String TAG = DemoActivity.class.getSimpleName();
    private static final String KEY_INFO_VIEW_TEXT = "INFO_VIEW_TEXT";
    private static final String KEY_ERROR_VIEW_TEXT = "ERROR_VIEW_TEXT";
    private ActivityDemoBinding mViewBinding;

    @Nullable private iSimpleAudioRecorderCallbacks mSimpleAudioRecorderCallbacks;
    @Nullable private SimpleAudioRecorder mSimpleAudioRecorder;
    @Nullable private String mRecordingFilePath;


    // Системные методы
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewBinding = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        mViewBinding.startButton.setOnClickListener(v ->
                DemoActivityPermissionsDispatcher.onStartButtonClickedWithPermissionCheck(this));
        mViewBinding.stopButton.setOnClickListener(this::onStopButtonClicked);
        mViewBinding.playButton.setOnClickListener(this::onPlayButtonClicked);

        mSimpleAudioRecorderCallbacks = new iSimpleAudioRecorderCallbacks() {
            @Override
            public void onRecordingStarted() {
//                Log.d(TAG, "onRecordingStarted()");
            }

            @Override
            public void onRecordingFinished(@NonNull String filePath) {
                mRecordingFilePath = filePath;
                showInfo("Записано в файл: "+filePath);
                hideAmplitude();
            }

            @Override
            public void onRecordingError(@NonNull String errorMsg) {
                showError(errorMsg);
            }

            @Override
            public void onAmplitudeChanged(double value) {
                showAmplitude(value);
            }
        };

        mRecordingFilePath = getCacheDir() + "/" + new Random().nextInt(10)+1 + ".wav";

        mSimpleAudioRecorder = new SimpleAudioRecorder(this, mSimpleAudioRecorderCallbacks);

//        getLifecycle().addObserver((DefaultLifecycleObserver) mSimpleAudioRecorder);

        if (null != savedInstanceState) {
            showInfo(savedInstanceState.getString(KEY_INFO_VIEW_TEXT));
            showError(savedInstanceState.getString(KEY_ERROR_VIEW_TEXT));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_INFO_VIEW_TEXT, mViewBinding.infoView.getText().toString());
        outState.putString(KEY_ERROR_VIEW_TEXT, mViewBinding.errorView.getText().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DemoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    // "Кнопочные" методы
    @NeedsPermission({ Manifest.permission.RECORD_AUDIO })
    public void onStartButtonClicked() {

        if (null != mSimpleAudioRecorder && mSimpleAudioRecorder.isRecordingNow()) {
            showInfo("Запись уже идёт");
            return;
        }

        if (MicUtils.isMicAvailable(this)) {
            hideInfo();
            hideError();
            if (null != mSimpleAudioRecorder)
                mSimpleAudioRecorder.startRecording(mRecordingFilePath);
        }
        else {
            showError("Микрофон недоступен");
        }
    }

    private void onStopButtonClicked(View view) {
        if(null != mSimpleAudioRecorder)
            mSimpleAudioRecorder.stopRecording();
    }

    private void onPlayButtonClicked(View view) {
        if (null != mRecordingFilePath) {
            GaplessAudioPlayer gaplessAudioPlayer = new GaplessAudioPlayer(new iAudioPlayer.Callbacks() {
                @Override
                public void onStarted(@NonNull SoundItem soundItem) {
                    mViewBinding.progressBar.animate().alpha(1f);
                }

                @Override
                public void onStopped() {
                    mViewBinding.progressBar.animate().alpha(0f);
                }

                @Override
                public void onPaused() {

                }

                @Override
                public void onResumed() {

                }

                @Override
                public void onProgress(int position, int duration) {
                    mViewBinding.progressBar.setMax(duration);
                    mViewBinding.progressBar.setProgress(position);
                }

                @Override
                public void onNoNextTracks() {

                }

                @Override
                public void onNoPrevTracks() {

                }

                @Override
                public void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {

                }

                @Override
                public void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {

                }

                @Override
                public void onCommonError(@NonNull ErrorCode errorCode, @Nullable String errorDetails) {

                }
            });

            gaplessAudioPlayer.play(new SoundItem("r1","Записанный звук", mRecordingFilePath));
        }
    }


    // Вспомогательные методы
    @OnPermissionDenied({ Manifest.permission.RECORD_AUDIO })
    public void onRecordAudioPermissionDenied() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied_dialog_title)
                .setMessage(R.string.permission_denied_dialog_message)
                .setIcon(R.drawable.ic_mic_unavailable)
                .setPositiveButton(
                        R.string.permission_denied_dialog_positive_button_text,
                        (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                            openAppPermissionsScreen();
                        })
                .setNegativeButton(R.string.permission_denied_dialog_negative_button_text,
                        (DialogInterface.OnClickListener) (dialogInterface, i) -> {

                        })
                .create()
                .show();
    }



    // Внутренние методы
    private void openAppPermissionsScreen() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    private void showAmplitude(double amplitudeValue) {
        if (amplitudeValue > 100d)
            return;

        if (amplitudeValue < 0)
            return;

        mViewBinding.amplitudeView.setText(String.valueOf(amplitudeValue));
        mViewBinding.levelView.setLevel((int) amplitudeValue);
    }

    private void hideAmplitude() {
        mViewBinding.amplitudeView.setText("");
        mViewBinding.levelView.setLevel(0);
    }

    private void showInfo(String text) {
        mViewBinding.infoView.setText(text);
    }

    private void hideInfo() {
        mViewBinding.infoView.setText("");
    }

    private void showError(String message) {
        mViewBinding.errorView.setText(message);
    }

    private void hideError() {
        mViewBinding.errorView.setText("");
    }

}