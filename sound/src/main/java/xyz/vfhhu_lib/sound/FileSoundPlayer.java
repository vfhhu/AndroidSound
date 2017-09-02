package xyz.vfhhu_lib.sound;

import android.media.AudioFormat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import xyz.vfhhu_lib.sound.header.HeaderWav;
import xyz.vfhhu_lib.sound.listener.OnFileSoundPlayerListener;

/**
 * Created by leo on 2017/9/2.
 */

public class FileSoundPlayer {
    private static int sampleRate = 4000;                          // 采样率  4000 每秒钟采集4000个点
    private static int channel = AudioFormat.CHANNEL_OUT_MONO;     // 声道个数 1 单声道
    private static int format = AudioFormat.ENCODING_PCM_8BIT;     // 每个采样点8bit量化 采样精度
    private static File input_file;
    private static boolean isPlaying=false;
    private static int header_length=0;
    private static int play_length=0;
    static PCMPlayer player;
    private static OnFileSoundPlayerListener l=null;
    public FileSoundPlayer() {
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void loadPcmFile(File f){
        input_file=f;
        header_length=0;
    }
    public static void loadPcmFile(File f,int sampleRate, int channel, int format){
        input_file=f;
        header_length=0;
    }
    public static void loadWavFile(File f){
        try {
            input_file=f;
            header_length=48;



            java.io.FileInputStream inputs= new java.io.FileInputStream( f );
            HeaderWav header= new HeaderWav(inputs);
            play_length=header.getLength()-40;

            //Log.d("-----",header.getSampleRate()+","+header.getChannel()+","+header.getDeep()+","+header.getFormat());

            FileSoundPlayer.sampleRate = header.getSampleRate();
            if(header.getChannel()==1)FileSoundPlayer.channel=AudioFormat.CHANNEL_OUT_MONO;
            if(header.getChannel()==2)FileSoundPlayer.channel=AudioFormat.CHANNEL_OUT_STEREO;

            //AudioFormat.ENCODING_DEFAULT
            FileSoundPlayer.format=AudioFormat.ENCODING_PCM_16BIT;
            if(header.getDeep()==8)FileSoundPlayer.format=AudioFormat.ENCODING_PCM_8BIT;
            if(header.getDeep()==16)FileSoundPlayer.format=AudioFormat.ENCODING_PCM_16BIT;
            if(header.getDeep()==32)FileSoundPlayer.format=AudioFormat.ENCODING_PCM_FLOAT;
            //FileSoundPlayer.format=AudioFormat.ENCODING_PCM_16BIT;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void loadWavFile(File f,int sampleRate, int channel, int format){
        try {
            input_file=f;
            header_length=48;
            java.io.FileInputStream inputs= new java.io.FileInputStream( f );
            HeaderWav header= new HeaderWav(inputs);
            play_length=header.getLength()-40;

            FileSoundPlayer.sampleRate = sampleRate;
            FileSoundPlayer.channel = channel;
            FileSoundPlayer.format = format;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void play(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                isPlaying=true;
                //Log.d("-----",sampleRate+","+channel+","+format);
                player=new PCMPlayer(sampleRate,channel,format);
                int total = 0;
                int nRead = 0;
                //Log.d("-----",player.getBufferSize()+"");
                byte[] buffer = new byte[player.getBufferSize()];
                if(input_file!=null){
                    try {
                        java.io.FileInputStream inputs= new java.io.FileInputStream( input_file );
                        try {
                            if(l!=null)l.onStart();
                            if(header_length>0)inputs.read(new byte[header_length]);
                            while((nRead = inputs.read(buffer)) != -1 && isPlaying) {
                                player.write( buffer,nRead);
                                total += nRead;
                                if(play_length>0 && total>=play_length){break;}
                            }
                            if(l!=null)l.onCompletion();
                        } catch (IOException e) {
                            if(l!=null)l.onError(e);
                            e.printStackTrace();
                        }
                        inputs.close();
                    } catch (Exception e) {
                        if(l!=null)l.onError(e);
                        e.printStackTrace();
                    }
                }
                stop();
            }
        }).start();

    }
    public static void stop(){
        isPlaying=false;
        if(l!=null)l.onStop();
        if(player!=null){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            player.destoryPlay();
        }
    }

    public static void setL(OnFileSoundPlayerListener listener) {
        l = listener;
    }
}
