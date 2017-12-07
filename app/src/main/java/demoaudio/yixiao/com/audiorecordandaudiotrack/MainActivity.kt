package demoaudio.yixiao.com.audiorecordandaudiotrack

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*




class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_CODE:Int = 1000
    private var isStart:Boolean = false
    private var isRecording:Boolean = false
    private var time = 1
    private val frequency = 11025
    private var filePath:String = ""
    var permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var handler = object:Handler(){
       override fun handleMessage(msg:Message){
           super.handleMessage(msg)
           if(msg.what == 200 && isRecording){
               time_tv.text = " 录制时长："+ time++
               sendTime()
           }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start.setOnClickListener(this@MainActivity)
        play.setOnClickListener(this@MainActivity)


    }
    override fun onClick(view: View) {
        when(view.id){
            R.id.start -> start()
            R.id.play -> play()
        }
    }
    private fun start(){
        if (VersionUtils.checkSDKVersion(23)) {
            LogUtil.i("TAG", "checkSDKVersion(23)")
            val flag = PermissionUtils.checkPermissionAllGranted(this,permissions)
            LogUtil.i("TAG", "checkPermissionAllGranted)" + flag)
            if (!flag) {
                PermissionUtils.RequestPermissionsRequestCodeValidator(this@MainActivity, permissions, REQUEST_CODE)
                return
            }
        }
        if(isStart){
            showToast("stop")
            start.text = "开始录音"
            isRecording = !isRecording
            path_tv.text = filePath
        }else{
            showToast("start")
            start.text = "结束录音"
            sendTime()

            Thread(){
                kotlin.run {
                    record()
                }
            }.start()
        }
        isStart = !isStart
    }
    fun sendTime(){
        handler.sendEmptyMessageDelayed(200,1000)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            var isAllGranted = true

            // 判断是否所有的权限都已经授予了
            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false
                    break
                }
            }

            if (!isAllGranted) {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
               showToast("您拒绝权限申请会导致部分功能无法正常使用，请在设置中将权限设置为允许！")
            }
        }
    }
    /**
     * 播放
     */
    private fun play(){
        showToast("play")
        Thread(){
            kotlin.run {
                // Get the file we want toplayback.
                val file = File(Environment.getExternalStorageDirectory().absolutePath + "/reverseme.pcm")
                // Get the length of the audio stored in the file(16 bit so 2 bytes per short)
                // and create a short array to store the recordedaudio.
                val musicLength = (file.length() / 2).toInt()
                val music = ShortArray(musicLength)
                LogUtil.e("music:"+music)


                try {
                    // Create a DataInputStream to read the audio databack from the saved file.
                    val fis = FileInputStream(file)
                    val bis = BufferedInputStream(fis)
                    val dis = DataInputStream(bis)

                    // Read the file into the musicarray.
                    var i = 0
                    while (dis.available() > 0) {
                        music[i] = dis.readShort()
                        i++
                    }
                    // Close the input streams.
                    dis.close()
                    /**
                    构造方法
                    - streamType：音频流的类型
                    AudioManager.STREAM_VOICE_CALL:电话的音频流
                    AudioManager.STREAM_SYSTEM:系统的音频流
                    AudioManager.STREAM_RING:闹钟
                    AudioManager.STREAM_MUSIC:音乐
                    AudioManager.STREAM_ALARM:警告声
                    AudioManager.STREAM_NOTIFICATION:通知
                    - sampleRateInHz：来源的音频的采样频率，单位Hz
                    - channelConfig：音频声道的配置
                    AudioFormat.CHANNEL_OUT_MONO:单声道输出(左)
                    AudioFormat.CHANNEL_OUT_STEREO:立体声输出(左和右)
                    - audioFormat：音频格式
                    AudioFormat.ENCODING_INVALID：无效的编码格式
                    AudioFormat.ENCODING_DEFAULT：默认的编码格式
                    AudioFormat.ENCODING_PCM_16BIT：每份采样数据为PCM 16bit，保证所有设备支持
                    AudioFormat.ENCODING_PCM_8BIT：样本数据格式为PCM 8bit，不保证所有设备支持
                    AudioFormat.ENCODING_PCM_FLOAT：单精度浮点样本
                    ...
                    - bufferSizeInBytes：缓冲区的大小
                    该缓冲区是为了存放需要回放的音频流数据，单位为字节。AudioTrack实例不断的从该缓冲区内读取写入的音频流数据，然后播放出来。它的大小应该是框架层尺寸的数倍。
                    如果该声轨的创建模式是"AudioTrack.MODE_STATIC"，
                    - mode：流或者是静态缓存
                    AudioTrack.MODE_STATIC:创建模式-在音频开始播放之前，音频数据仅仅只会从Java层写入到本地层中一次。即开始播放前一次性写入音频数据。
                    AudioTrack.MODE_STREAM:创建模式-在音频播放的时候，音频数据会同时会以流的形式写入到本地层中。即一边播放，一边写入数据。(很明显，如果实现一边录音一边播放的话，用这个模式创建声轨)
                     */
                    val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                            frequency,
                            AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            musicLength * 2,
                            AudioTrack.MODE_STREAM)
                    // Start playback
                    audioTrack.play()
                    // Write the music buffer to the AudioTrackobject
                    audioTrack.write(music, 0, musicLength)

                    audioTrack.stop()
                } catch (t: Throwable) {
                    Log.e("AudioTrack", "Playback Failed")
                }

            }
        }.start()

    }
    private fun showToast(msg:String){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
    }

    private fun record() {

        val channelConfiguration = AudioFormat.CHANNEL_IN_MONO
        val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        val file = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm")

        // Delete any previousrecording.
        if (file.exists())
            file.delete()


        // Create the new file.
        try {
            file.createNewFile()
        } catch (e: IOException) {
            throw IllegalStateException("Failed to create " + file.toString())
        }
        filePath = file.absolutePath
        try {
            // Create a DataOuputStream to write the audiodata into the saved file.
            val os = FileOutputStream(file)
            val bos = BufferedOutputStream(os)
            val dos = DataOutputStream(bos)

            // Create a new AudioRecord object to record theaudio.获取我们要创建的AudioRecord实例所需要的缓冲区的最小长度
            val bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding)
            /**
            构造方法 创建AudioRecord实例
            - 由于一些无效的参数或者其他错误会抛出IllegalArgumentException，所以在你构造了一个AudioRecord实例之后，需要立刻调用 getState() 方法来判断这个实例的状态是否可以使用。
            参数：
            - audioSource：录音来源，查看MediaRecorder.AudioSource类对录音来源的定义。
            MediaRecorder.AudioSource.DEFAULT   默认音频来源
            MediaRecorder.AudioSource.MIC       麦克风
            MediaRecorder.AudioSource.VOICE_UPLINK  上行线路的声音来源
            ...
            一般情况下，我们使用麦克风即可。
            - sampleRateInHz：采样率，单位Hz(赫兹)。44100Hz是目前唯一一个能够在所有的设备上使用的频率，但是一些其他的例如22050、16000、11025也能够在一部分设备上使用。
            - channelConfig：音频声道的配置(输入)
                AudioFormat.CHANNEL_IN_MONO：单声道
                AudioFormat.CHANNEL_IN_STEREO：立体声
            其中，CHANNEL_IN_MONO可以保证在所有设备上使用。
            - audioFormat：返回的音频数据的编码格式
                AudioFormat.ENCODING_INVALID：无效的编码格式
                AudioFormat.ENCODING_DEFAULT：默认的编码格式
                AudioFormat.ENCODING_PCM_16BIT：每份采样数据为PCM 16bit，保证所有设备支持
                AudioFormat.ENCODING_PCM_8BIT：样本数据格式为PCM 8bit，不保证所有设备支持
                AudioFormat.ENCODING_PCM_FLOAT：单精度浮点样本
            ...
            - bufferSizeInBytes：在录音时期，音频数据写入的缓冲区的整体大小(单位字节)，即缓冲区的大小。
            我们能够从这个缓冲区中读取到不超过缓冲区长度的整块数据。可以通过 getMinBufferSize(int, int, int)
            这个方法来决定我们使用的AudioRecord实例所需要的最小的缓冲区的大小，如果我们使用的数值比这个还要小，则会导致AudioRecord实例初始化失败。
             */
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, channelConfiguration,
                    audioEncoding, bufferSize)

            LogUtil.e("audioRecord.state:"+audioRecord.state)
            val buffer = ShortArray(bufferSize)
//            调用该方法后，会调用本地方法，使得录音设备开始录音
            audioRecord.startRecording()

           isRecording = true
            while (isRecording) {
                val bufferReadResult = audioRecord.read(buffer, 0, bufferSize)
                for (i in 0 until bufferReadResult)
                    dos.writeShort(buffer[i].toInt())
            }
            /**
            停止录音
            - 调用本地方法会使得硬件设备停止录音，设置当前录音状态为停止录音。
             */
            audioRecord.stop()
            /**
                释放资源
                - 释放本地的AudioRecord资源，AudioRecord实例就不能够使用了，
                在调用完该方法之后，引用应该被置为null。
             */
            audioRecord.release()
            dos.close()

        } catch (t: Throwable) {
            Log.e("AudioRecord", "Recording Failed")
        }
    }

}
