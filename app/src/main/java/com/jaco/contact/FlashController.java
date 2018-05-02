package com.jaco.contact;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;

/**
 * Created by osvel on 8/23/16.
 */
public class FlashController {

    private static FlashController instance;

    private Camera mCamera;

    private boolean turnOn;
    private boolean init;

    public static FlashController getInstance(){
        if (instance == null)
            instance = new FlashController();

        return instance;
    }

    public FlashController() {
    }

    public void startFlashing(Context context){
//        initCamera();
        context.startService(new Intent(context, FlashService.class));
    }

    public void stopFlashing(){
        releaseCamera();
    }

    public boolean isInit() {
        return init;
    }

    public boolean isTurnOn(){
        return !turnOn;
    }

    public void switchFlash(){

        if (turnOn && init){
            flashOn();
            turnOn = false;
        }
        else if (init) {
            flashOff();
            turnOn = true;
        }

    }

    private void flashOff() {
        try {
            Camera.Parameters params = this.mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            this.mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void flashOn() {
        try {
            Camera.Parameters params = this.mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            this.mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCamera() {

        try {
            if (this.mCamera == null) {
                this.mCamera = Camera.open();
                this.mCamera.startPreview();
            }
            init = true;
        } catch (Exception e) {
            init = false;
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        if (this.mCamera != null) {
            try {
                this.mCamera.stopPreview();
                this.mCamera.setPreviewDisplay(null);
                this.mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mCamera = null;
        }
        init = false;
    }

}
