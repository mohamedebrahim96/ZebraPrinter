package com.jeluchu.zebraprinter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.barteksc.pdfviewer.util.FileUtils.fileFromAsset
import com.shockwave.pdfium.PdfiumCore
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.device.ZebraIllegalArgumentException
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    //private var printerConnection: BluetoothPrinterConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isStoragePermissionGranted()
        btnPrint.setOnClickListener {
            try {
                //connect()
                //createPdf()
                rotatePDF()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted")
                true
            } else {
                Log.v("TAG", "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted")
            true
        }
    }

//    private fun connect() {
//
//        printerConnection = null
//        printerConnection = BluetoothPrinterConnection("00:07:4D:4E:70:9E")
//
//        try { printerConnection!!.open() } catch (e: ZebraPrinterConnectionException) { disconnect() }
//
//        val printer: ZebraPrinter
//
//        if (printerConnection!!.isConnected) {
//            try {
//                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.ZPL, printerConnection)
//                printer.graphicsUtil.printImage(BitmapFactory.decodeResource(this.resources, R.drawable.parcel_recived), 0, 0, 550, 2030, false)
//            } catch (e: ZebraPrinterConnectionException) { disconnect() }
//        }
//    }
//
//    private fun disconnect() {
//        try { if (printerConnection != null) printerConnection!!.close() }
//        catch (e: ZebraPrinterConnectionException) { e.message }
//    }

    private fun connect(filePath: String) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val connection: Connection =
                TcpConnection("192.168.1.134", 6101)
        try {
            connection.open()
            val printer: ZebraPrinter = ZebraPrinterFactory.getInstance(connection)
            //printer.sendFileContents("/sdcard/Download/Downloads/shipexpresslabel.pdf")
            printer.sendFileContents(filePath)
        } catch (e: ConnectionException) {
            e.printStackTrace()
        } catch (e: ZebraPrinterLanguageUnknownException) {
            e.printStackTrace()
        } catch (e: ZebraIllegalArgumentException) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
    }


//    @RequiresApi(Build.VERSION_CODES.KITKAT)
//    private fun createPdf2() {
//        val document = PdfDocument()
//        val pageInfo = PageInfo.Builder(100, 100, 1).create()
//        val page: PdfDocument.Page = document.startPage(pageInfo)
//        val content: View = getContentView()
//        content.draw(page.canvas)
//        document.finishPage(page)
//        document.writeTo(FileOutputStream(File("/sdcard/Download/Downloads/sample2.pdf")))
//        document.close()
//    }


    private fun rotatePDF() {
        val document = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PdfDocument()
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }
        val pageInfo: PageInfo = PageInfo.Builder(612, 792, 1).create()
        val page: PdfDocument.Page = document.startPage(pageInfo)
        val canvas = page.canvas
//        val paint = Paint()
//        paint.textSize = 30f
        val icon = BitmapFactory.decodeResource(this.resources,
                R.drawable.ticket)
//        val iconResize =
//                getResizedBitmap(generateImageFromPdf("shipexpresslabel.pdf", 0, 559, 838)!!,
//                        374, 250)

        val bitmapImage = BitmapFactory.decodeFile("/sdcard/Download/Downloads/shipexpresslabel.png")
        val iconResize = RotateBitmap(generateImageFromPdf(
                "sampleparcelid2.pdf", 0, 612, 792)!!,
                270f)

//        val iconResize =
//                getResizedBitmap(generateImageFromPdf(
//                        "Parcel_dropped.pdf", 0, 612, 792)!!,
//                        150,150)

        canvas.drawBitmap(iconResize!!, 0f, 0f, null)
        document.finishPage(page)

        // write the document content
        val directoryPath = "/sdcard/Download/Downloads/"//Environment.getExternalStorageDirectory().getPath() + "/usbpdf/"
        val file = File(directoryPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        val targetPdf = directoryPath + "sample2.pdf"
        val filePath = File(targetPdf)
        try {
            document.writeTo(FileOutputStream(filePath))
            connect(filePath.path)
            Toast.makeText(this, "Document Created Successfully", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Log.e("main", "error " + e.toString())
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show()
        }
        document.close()
    }
//    private fun createPdf() {
//        val document = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            PdfDocument()
//        } else {
//            TODO("VERSION.SDK_INT < KITKAT")
//        }
//        val pageInfo: PageInfo = PageInfo.Builder(559, 838, 1).create()
//        val page: PdfDocument.Page = document.startPage(pageInfo)
//        val canvas = page.canvas
////        val paint = Paint()
////        paint.textSize = 30f
//        val icon = BitmapFactory.decodeResource(this.resources,
//                R.drawable.ticket)
////        val iconResize =
////                getResizedBitmap(generateImageFromPdf("shipexpresslabel.pdf", 0, 559, 838)!!,
////                        374, 250)
//
//    val bitmapImage = BitmapFactory.decodeFile("/sdcard/Download/Downloads/shipexpresslabel.png")
//    val iconResize = RotateBitmap(generateImageFromPdf(
//            "sampleparcelid2.pdf", 0, 612, 792)!!,
//            90f)
//
////        val iconResize =
////                getResizedBitmap(generateImageFromPdf(
////                        "Parcel_dropped.pdf", 0, 612, 792)!!,
////                        150,150)
//
//        canvas.drawBitmap(iconResize!!, 0f, 0f, null)
//        document.finishPage(page)
//
//        // write the document content
//        val directoryPath = "/sdcard/Download/Downloads/"//Environment.getExternalStorageDirectory().getPath() + "/usbpdf/"
//        val file = File(directoryPath)
//        if (!file.exists()) {
//            file.mkdirs()
//        }
//        val targetPdf = directoryPath + "sample2.pdf"
//        val filePath = File(targetPdf)
//        try {
//            document.writeTo(FileOutputStream(filePath))
//            connect(filePath.path)
//            Toast.makeText(this, "Document Created Successfully", Toast.LENGTH_LONG).show()
//        } catch (e: IOException) {
//            Log.e("main", "error " + e.toString())
//            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show()
//        }
//        document.close()
//    }

    private fun generateImageFromPdf(assetFileName: String, pageNumber: Int, width: Int, height: Int): Bitmap? {
        val pdfiumCore = PdfiumCore(this)
        try {
            val f: File = fileFromAsset(this, assetFileName)
            val fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfDocument: com.shockwave.pdfium.PdfDocument? = pdfiumCore.newDocument(fd)
            pdfiumCore.openPage(pdfDocument, pageNumber)
            //int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            //int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height)
            //saveImage(bmp, filena);
            pdfiumCore.closeDocument(pdfDocument)
            return bmp
        } catch (e: java.lang.Exception) {
            //todo with exception
        }
        return null
    }



    private fun resize(imaged: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
        var image = imaged
        if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > 1) {
                finalWidth = Math.round(maxHeight.toFloat() * ratioBitmap)
            } else {
                finalHeight = Math.round(maxWidth.toFloat() / ratioBitmap)
            }
            return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, false).also { image = it }
        }
        return image
    }

    fun RotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * 0.9).toInt(),
                (bitmap.height * 0.9).toInt(), true)
    }

    fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap? {
        // GET CURRENT SIZE
        val width = bm.width
        val height = bm.height
        // GET SCALE SIZE
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)
        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }

    private fun resize22(bitmapImage: Bitmap):Bitmap{
        val nh = (bitmapImage.height * (300.0 / bitmapImage.width)).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmapImage, 300, nh, true)
        return scaled
    }

}
