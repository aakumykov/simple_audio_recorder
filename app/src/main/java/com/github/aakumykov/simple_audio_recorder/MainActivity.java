package com.github.aakumykov.simple_audio_recorder;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.aakumykov.simple_audio_recorder.databinding.ActivityMainBinding;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements AudioRecorder.Callbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding mBinding;
    private SimpleAudioRecorder mSimpleAudioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareLayout();
        prepareRecorder();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    private void onRecordButtonClicked(View view) {
        if (mSimpleAudioRecorder.isRecordingNow())
            mSimpleAudioRecorder.stopRecording();
        else
            MainActivityPermissionsDispatcher.startRecordingWithPermissionCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void startRecording() {

        final String recordFileName = "sound_record.wav";
        final File targetFile = new File(getCacheDir(), recordFileName);

        try {
            mSimpleAudioRecorder.startRecording(targetFile);
        }
        catch (Exception e) {
            showError(e);
        }
    }


    // AudioRecorder.Callbacks
    @Override
    public void onRecordingStarted() {
        showStopRecordingButton();
        showAmplitudeView();
        hideError();
    }

    @Override
    public void onRecordingFinished(@NonNull File recordedFile) {
        showStartRecordingButton();
        hideAmplitudeView();
    }

    @Override
    public void onRecordingError(@NonNull Exception e) {
        showError(e);
        hideAmplitudeView();
    }

    @Override
    public void onSoundAmplitudeChanged(double amplitudeValue) {
        displayAmplitude(amplitudeValue);
    }



    private void prepareRecorder() {
        mSimpleAudioRecorder = new SimpleAudioRecorder();
        mSimpleAudioRecorder.setCallbacks(this);
    }

    private void prepareLayout() {
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.recordButton.setOnClickListener(this::onRecordButtonClicked);
    }



    private void showStopRecordingButton() {
        mBinding.recordButton.setImageResource(R.drawable.ic_record_stop);
    }

    private void showStartRecordingButton() {
        mBinding.recordButton.setImageResource(R.drawable.ic_record_start);
    }

    private void showError(Exception e) {
        Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
        showError(ExceptionUtils.getErrorMessage(e));
    }

    private void showError(String text) {
        mBinding.errorView.setText(text);
        mBinding.errorView.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        mBinding.errorView.setText("");
        mBinding.errorView.setVisibility(View.GONE);
    }

    private void showAmplitudeView() {
        mBinding.amplitudeView.animate()
                .alpha(1f)
                .setDuration(200)
                .withStartAction(() -> {
                    mBinding.amplitudeView.setAlpha(0f);
                    mBinding.amplitudeView.setVisibility(View.VISIBLE);
                })
                .start();
    }

    private void hideAmplitudeView() {
        mBinding.amplitudeView.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction(() -> {
                    mBinding.amplitudeView.setVisibility(View.GONE);
                })
                .start();
    }

    private void displayAmplitude(double amplitudeValue) {
        mBinding.amplitudeView.setProgress((int) amplitudeValue);
    }


    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    void onRecordAudioPermissionDenied() {
        showError(new RuntimeException("Разрешите записывать звук"));
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    void onNeverAskAgain() {
        showError(new RuntimeException("Запись звука запрещена"));
    }
}