/*
 * Copyright (C) 2008 ZXing authors
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.anicodebreaker.intest1;


import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.example.anicodebreaker.intest1.camera.CameraManager;

final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private static State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW,
        PREVIEW_PAUSED,
        CONTINUOUS,
        CONTINUOUS_PAUSED,
        SUCCESS,
        DONE
    }

    CaptureActivityHandler(CaptureActivity activity, CameraManager cameraManager, boolean isContinuousModeActive) {
        this.activity = activity;
        this.cameraManager = cameraManager;

        // Start ourselves capturing previews (and decoding if using continuous recognition mode).
        cameraManager.startPreview();

        decodeThread = new DecodeThread(activity);
        decodeThread.start();

        if (isContinuousModeActive) {
            state = State.CONTINUOUS;

            // Show the shutter and torch buttons
            activity.setButtonVisibility(true);

            // Display a "be patient" message while first recognition request is running
            activity.setStatusViewForContinuous();

            restartOcrPreviewAndDecode();
        } else {
            state = State.SUCCESS;

            // Show the shutter and torch buttons
            activity.setButtonVisibility(true);

            restartOcrPreview();
        }
    }

    @Override
    public void handleMessage(Message message) {

        switch (message.what) {
            case R.id.restart_preview:
                restartOcrPreview();
                break;
            case R.id.ocr_continuous_decode_failed:
                DecodeHandler.resetDecodeState();
                try {
                    activity.handleOcrContinuousDecode((OcrResultFailure) message.obj);
                } catch (NullPointerException e) {
                }
                if (state == State.CONTINUOUS) {
                    restartOcrPreviewAndDecode();
                }
                break;
            case R.id.ocr_continuous_decode_succeeded:
                DecodeHandler.resetDecodeState();
                try {
                    activity.handleOcrContinuousDecode((OcrResult) message.obj);
                } catch (NullPointerException e) {
                    // Continue
                }
                if (state == State.CONTINUOUS) {
                    restartOcrPreviewAndDecode();
                }
                break;
            case R.id.ocr_decode_succeeded:
                state = State.SUCCESS;
                activity.setShutterButtonClickable(true);
                activity.handleOcrDecode((OcrResult) message.obj);
                break;
            case R.id.ocr_decode_failed:
                state = State.PREVIEW;
                activity.setShutterButtonClickable(true);
                Toast toast = Toast.makeText(activity.getBaseContext(), "OCR failed. Please try again.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                break;
        }
    }

    void stop() {
        state = State.CONTINUOUS_PAUSED;
        removeMessages(R.id.ocr_continuous_decode);
        removeMessages(R.id.ocr_decode);
        removeMessages(R.id.ocr_continuous_decode_failed);
        removeMessages(R.id.ocr_continuous_decode_succeeded);

    }

    void resetState() {
        if (state == State.CONTINUOUS_PAUSED) {
            state = State.CONTINUOUS;
            restartOcrPreviewAndDecode();
        }
    }

    void quitSynchronously() {
        state = State.DONE;
        if (cameraManager != null) {
            cameraManager.stopPreview();
        }
        try {
            decodeThread.join(500L);
        } catch (InterruptedException e) {
        } catch (RuntimeException e) {
        } catch (Exception e) {
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.ocr_continuous_decode);
        removeMessages(R.id.ocr_decode);

    }

    private void restartOcrPreview() {
        activity.setButtonVisibility(true);

        if (state == State.SUCCESS) {
            state = State.PREVIEW;

            // Draw the viewfinder.
            activity.drawViewfinder();
        }
    }

    private void restartOcrPreviewAndDecode() {
        // Continue capturing camera frames
        cameraManager.startPreview();

        // Continue requesting decode of images
        cameraManager.requestOcrDecode(decodeThread.getHandler(), R.id.ocr_continuous_decode);
        activity.drawViewfinder();
    }

    private void ocrDecode() {
        state = State.PREVIEW_PAUSED;
        cameraManager.requestOcrDecode(decodeThread.getHandler(), R.id.ocr_decode);
    }

    void hardwareShutterButtonClick() {
        if (state == State.PREVIEW) {
            ocrDecode();
        }
    }

    void shutterButtonClick() {
        activity.setShutterButtonClickable(false);
        ocrDecode();
    }

}
