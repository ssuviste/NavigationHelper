package ee.iti0213.navigationhelper.controller

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class CompassController(
    private var sensorManager: SensorManager,
    private var accelerometer: Sensor,
    private var magnetometer: Sensor,
    private var image: ImageView
) : SensorEventListener {

    private var currentDegree = 0.0f
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private var visible = false


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (visible) {
            if (event.sensor === accelerometer) {
                lowPass(event.values, lastAccelerometer)
                lastAccelerometerSet = true
            } else if (event.sensor === magnetometer) {
                lowPass(event.values, lastMagnetometer)
                lastMagnetometerSet = true
            }

            if (lastAccelerometerSet && lastMagnetometerSet) {
                val r = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val degree = (Math.toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360

                    val rotateAnimation = RotateAnimation(
                        currentDegree,
                        -degree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    rotateAnimation.duration = 1000
                    rotateAnimation.fillAfter = true

                    image.startAnimation(rotateAnimation)
                    currentDegree = -degree
                }
            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    fun actOnPause() {
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
    }

    fun actOnResume() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun setVisible(visible: Boolean) {
        this.visible = visible
    }
}