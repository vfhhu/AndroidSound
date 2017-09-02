package xyz.vfhhu_lib.sound.listener;

/**
 * Created by leo on 2017/9/2.
 */

public interface OnFileSoundPlayerListener {
    void onCompletion();
    void onStart();
    void onStop();
    void onError(Exception e);
}
