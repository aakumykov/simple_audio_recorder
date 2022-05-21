package com.github.aakumykov.simple_audio_recorder_module;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.github.aakumykov.simple_audio_recorder_module.recorder_service.RecorderService;
import com.github.aakumykov.simple_audio_recorder_module.recorder_service.RecorderServiceBinder;
import com.github.aakumykov.simple_audio_recorder_module.recorder_service.iRecorderServiceCallbacks;

// TODO: логгирование ошибки, если проигрывателю передаётся пустой путь

/*
Пути дальнейшего развития:
1) многократный запуск рекодера без остановки службы;
2) вызов коллбека "завершено" по месту завершения записи, а не
разрушения службы.
 */

public class SimpleAudioRecorder
        implements iSimpleAudioRecorder, DefaultLifecycleObserver
{
    public static final String TAG = "service_debug";

    @NonNull private final Intent mRecorderServiceIntent;
    @NonNull private final ServiceConnection mRecorderServiceConnection;
    @NonNull private final iRecorderServiceCallbacks mRecorderServiceCallbacks;
    @Nullable private RecorderService mRecorderService;

    @NonNull private final Context mContext;
    @NonNull private final iSimpleAudioRecorderCallbacks mCallbacks;
    @Nullable private String mFilePath;
    private boolean mBoundServiceWasCalled = false;


    // TODO: идентичность коллбеков...
    public SimpleAudioRecorder(@NonNull Context context,
                               @NonNull iSimpleAudioRecorderCallbacks callbacks) {

        mContext = context;
        mCallbacks = callbacks;

        mRecorderServiceIntent = new Intent(context, RecorderService.class);

        mRecorderServiceCallbacks = new iRecorderServiceCallbacks() {
            @Override
            public void onAmplitude(double amplitudeValue) {
                mCallbacks.onAmplitudeChanged(amplitudeValue);
            }

            @Override
            public void onRecordingStarted() {
                mCallbacks.onRecordingStarted();
            }

            @Override
            public void onRecordingFinished(@NonNull String filePath) {
                mFilePath = filePath;
            }

            @Override
            public void onRecordingError(@NonNull String errorMsg) {
                mCallbacks.onRecordingError(errorMsg);
            }

            @Override
            public void onRecorderServiceReleased() {
                /*SimpleAudioRecorder говорит, что отработал,
                когда сама служба остановилась, а не когда
                в ней остановилась запись. Это хак против
                позднего прихода значения амплитуды.*/
                mCallbacks.onRecordingFinished(mFilePath);
            }
        };

        mRecorderServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecorderService = ((RecorderServiceBinder) service).getRecorderService();
                mRecorderService.setCallbacks(mRecorderServiceCallbacks);

                if (!mRecorderService.isRecordingNow())
                    mRecorderService.startRecording();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                /* Здесь не нужно убирать коллбеки
                 * из службы, так как служба
                 * самоуничтожается. А в процессе этого
                 * уничтожения, их нужно ещё дёрнуть. */
//                Log.d(TAG, "onServiceDisconnected()");
                mRecorderService = null;
            }
        };
    }


    // TODO: а сейчас же он внутри ViewModel...
    // Жизненный цикл Activity
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {

        if (null != mRecorderService)
            throw new IllegalStateException("Служба записи уже привязана.");

        bindToService();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        unbindFromService();
    }


    // Главные методы
    @Override
    public void startRecording(@NonNull String filePath) {

        if (!mBoundServiceWasCalled)
            throw new IllegalStateException("Служба записи не привязана (mRecorderService == null)." +
                        "Необходимо подписать этот объект на жизненный цикл Activity или " +
                        "вызывать методы onStart() и onStop() внучную (в соотвутствующих методах ЖЦ Activity).");

        mRecorderServiceIntent.putExtra(RecorderService.EXTRA_FILE_PATH, filePath);

        mContext.startService(mRecorderServiceIntent);
    }

    @Override
    public void stopRecording() {
        if (null != mRecorderService)
            if (mRecorderService.isRecordingNow())
                mRecorderService.stopRecording();
    }

    @Override
    public boolean isRecordingNow() {
        if (null == mRecorderService)
            return false;
        else
            return mRecorderService.isRecordingNow();
    }


    // Внутренние методы
    private void bindToService() {
        mContext.bindService(mRecorderServiceIntent, mRecorderServiceConnection, 0);
        mBoundServiceWasCalled = true;
    }

    private void unbindFromService() {
        mContext.unbindService(mRecorderServiceConnection);
    }

}