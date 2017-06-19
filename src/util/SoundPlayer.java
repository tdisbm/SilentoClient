package util;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundPlayer {
    public static void playSound(File sound) {
        new Thread(
                () -> {
                    try {
                        Clip clip = AudioSystem.getClip();
                        clip.open(AudioSystem.getAudioInputStream(sound));
                        clip.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
    }
}
