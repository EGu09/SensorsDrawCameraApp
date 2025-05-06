package com.example.sensorsdrawcameraapp.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sensorsdrawcameraapp.databinding.FragmentSensorsBinding

class SensorsFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentSensorsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var magneticSensor: Sensor? = null // Changed from lightSensor to magneticSensor
    private var proximitySensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sensorsViewModel = ViewModelProvider(this).get(SensorsViewModel::class.java)

        _binding = FragmentSensorsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize sensor manager
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Initialize sensors
        initializeSensors()

        return root
    }

    private fun initializeSensors() {
        // Magnetic field sensor
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticSensor == null) {
            Toast.makeText(context, "Magnetic field sensor not available", Toast.LENGTH_SHORT).show()
            "Magnetic field sensor not available".also { binding.magneticValue.text = it }
        }

        // Proximity sensor
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (proximitySensor == null) {
            Toast.makeText(context, "Proximity sensor not available", Toast.LENGTH_SHORT).show()
            "Proximity sensor not available".also { binding.proximityValue.text = it }
        }

        // Accelerometer sensor
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(context, "Accelerometer not available", Toast.LENGTH_SHORT).show()
            "Accelerometer not available".also { binding.accelerometerValue.text = it }
        }
    }

    override fun onResume() {
        super.onResume()
        magneticSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximitySensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometerSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                "Magnetic Field:\nX: $x µT\nY: $y µT\nZ: $z µT\nMagnitude: $magnitude µT".also {
                    binding.magneticValue.text = it
                }
            }
            Sensor.TYPE_PROXIMITY -> {
                val proximity = event.values[0]
                "Proximity: $proximity cm".also { binding.proximityValue.text = it }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                "Accelerometer:\nX: $x\nY: $y\nZ: $z".also { binding.accelerometerValue.text = it }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used in this example
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}