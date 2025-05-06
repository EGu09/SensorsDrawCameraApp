package com.example.sensorsdrawcameraapp.ui.draw

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sensorsdrawcameraapp.databinding.FragmentDrawBinding

class DrawFragment : Fragment() {

    private var _binding: FragmentDrawBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private var originalBitmap: Bitmap? = null
    private var drawingBitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
    }

    private var lastX = 0f
    private var lastY = 0f

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Storage permissions required to access gallery", Toast.LENGTH_LONG).show()
            }
        }

    // Gallery launcher
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageUri = uri
                    loadImageAndFixRotation(uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val drawViewModel = ViewModelProvider(this).get(DrawViewModel::class.java)

        _binding = FragmentDrawBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupButtons()
        setupDrawingCanvas()

        return root
    }

    private fun setupButtons() {
        binding.selectImageButton.setOnClickListener {
            checkPermissionsAndOpenGallery()
        }

        binding.resetButton.setOnClickListener {
            resetToOriginal()
        }

        binding.saveButton.setOnClickListener {
            saveEditedImage()
        }

        // Color buttons
        binding.blackColorButton.setOnClickListener { paint.color = Color.BLACK }
        binding.redColorButton.setOnClickListener { paint.color = Color.RED }
        binding.greenColorButton.setOnClickListener { paint.color = Color.GREEN }
        binding.blueColorButton.setOnClickListener { paint.color = Color.BLUE }
    }

    private fun setupDrawingCanvas() {
        binding.drawingImageView.setOnTouchListener { view, event ->
            if (originalBitmap == null) {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnTouchListener false
            }

            // Get image dimensions and view dimensions
            val bitmap = drawingBitmap ?: return@setOnTouchListener false
            val viewWidth = binding.drawingImageView.width
            val viewHeight = binding.drawingImageView.height
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            // Calculate the actual drawing area within the ImageView
            val scale: Float
            var dx = 0f
            var dy = 0f

            if (bitmapWidth * viewHeight > viewWidth * bitmapHeight) {
                // Image is wider than view (relative to their heights)
                scale = viewWidth.toFloat() / bitmapWidth.toFloat()
                dy = (viewHeight - (bitmapHeight * scale)) * 0.5f
            } else {
                // Image is taller than view (relative to their widths)
                scale = viewHeight.toFloat() / bitmapHeight.toFloat()
                dx = (viewWidth - (bitmapWidth * scale)) * 0.5f
            }

            // Calculate the touch point in bitmap coordinates
            val touchX = (event.x - dx) / scale
            val touchY = (event.y - dy) / scale

            // Make sure the coordinates are within the bitmap bounds
            val boundedX = touchX.coerceIn(0f, bitmapWidth.toFloat())
            val boundedY = touchY.coerceIn(0f, bitmapHeight.toFloat())

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = boundedX
                    lastY = boundedY
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    canvas?.drawLine(lastX, lastY, boundedX, boundedY, paint)
                    lastX = boundedX
                    lastY = boundedY
                    binding.drawingImageView.invalidate()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }

    private fun checkPermissionsAndOpenGallery() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.all { permission ->
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
            }) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // New method to replace the original loadImage method
    private fun loadImageAndFixRotation(uri: Uri) {
        try {
            // Load the image as a bitmap
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Read EXIF orientation
            val exifInputStream = requireContext().contentResolver.openInputStream(uri)
            val exif = ExifInterface(exifInputStream!!)
            exifInputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            // Rotate bitmap according to EXIF orientation
            originalBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            // Create a mutable copy for drawing
            resetToOriginal()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper method to rotate bitmaps
    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun resetToOriginal() {
        originalBitmap?.let { bitmap ->
            drawingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            canvas = Canvas(drawingBitmap!!)
            binding.drawingImageView.setImageBitmap(drawingBitmap)
        }
    }

    private fun saveEditedImage() {
        if (drawingBitmap == null) {
            Toast.makeText(requireContext(), "No image to save", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "DrawEdit_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawApp")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                    drawingBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    requireContext().contentResolver.update(it, contentValues, null, null)
                }

                Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}