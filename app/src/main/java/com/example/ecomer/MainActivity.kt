package com.example.ecomer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecomer.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val productList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter
    private var productsListener: ListenerRegistration? = null

    // Trạng thái: null = đang thêm mới, non-null = đang sửa
    private var editingProduct: Product? = null

    // Phần B: ảnh đã chọn (Base64)
    private var selectedImageBase64: String = ""

    // Bộ chọn ảnh từ thiết bị
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val fileName = getFileName(it)
                binding.tvImageName.text = fileName
                selectedImageBase64 = compressAndEncode(it)
                showPreviewFromBase64(selectedImageBase64)
            } catch (e: Exception) {
                Toast.makeText(this, "Không thể đọc ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        checkUserRole()
        listenProducts()
    }

    override fun onDestroy() {
        super.onDestroy()
        productsListener?.remove()
    }

    // ================================================================
    //  SETUP
    // ================================================================

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            products = productList,
            onEdit = { product -> fillFormForEdit(product) },
            onDelete = { product -> confirmDelete(product) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        // Nút Thêm / Cập nhật
        binding.btnThem.setOnClickListener {
            if (editingProduct != null) {
                updateProduct()
            } else {
                addProduct()
            }
        }

        // Nút Hủy chế độ sửa
        binding.btnHuy.setOnClickListener {
            clearForm()
        }

        // Chọn ảnh (Phần B)
        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.layoutImagePicker.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Nút Đăng xuất (Phần C)
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // ================================================================
    //  PHẦN A — CRUD SẢN PHẨM TRÊN CLOUD FIRESTORE
    // ================================================================

    /**
     * A3: Hiển thị danh sách sản phẩm Realtime từ Firestore
     */
    private fun listenProducts() {
        productsListener = db.collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                snapshots?.documents?.forEach { doc ->
                    productList.add(Product.fromDocument(doc))
                }
                adapter.notifyDataSetChanged()

                // Cập nhật tiêu đề số lượng & empty state
                binding.tvListHeader.text = "Danh sách sản phẩm:"
                binding.tvEmpty.visibility =
                    if (productList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    /**
     * A2: Thêm sản phẩm mới vào Firestore
     */
    private fun addProduct() {
        val (ten, loai, gia) = validateInput() ?: return

        val product = Product(
            tenSanPham = ten,
            loaiSanPham = loai,
            gia = gia,
            image = selectedImageBase64
        )

        binding.btnThem.isEnabled = false

        db.collection("products").add(product.toFirestoreMap())
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.msg_add_success), Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi thêm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.btnThem.isEnabled = true
            }
    }

    /**
     * A4: Sửa sản phẩm — Bước 1: Đổ dữ liệu ngược lên form
     */
    private fun fillFormForEdit(product: Product) {
        editingProduct = product
        binding.apply {
            edtTen.setText(product.tenSanPham)
            edtLoai.setText(product.loaiSanPham)
            edtGia.setText(product.gia.toString())
            btnThem.text = getString(R.string.btn_cap_nhat)
            btnHuy.visibility = View.VISIBLE

            if (product.image.isNotEmpty()) {
                tvImageName.text = "image_${product.id.take(6)}.jpg"
                selectedImageBase64 = product.image
                showPreviewFromBase64(product.image)
            } else {
                tvImageName.text = getString(R.string.no_image)
                selectedImageBase64 = ""
                imgPreview.visibility = View.GONE
            }
            edtTen.requestFocus()
        }
    }

    /**
     * A4: Sửa sản phẩm — Bước 2: Cập nhật document trên Firestore
     */
    private fun updateProduct() {
        val (ten, loai, gia) = validateInput() ?: return
        val product = editingProduct ?: return

        val updatedProduct = Product(
            tenSanPham = ten,
            loaiSanPham = loai,
            gia = gia,
            image = selectedImageBase64
        )

        binding.btnThem.isEnabled = false

        db.collection("products").document(product.id).set(updatedProduct.toFirestoreMap())
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.msg_update_success), Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.btnThem.isEnabled = true
            }
    }

    /**
     * A5: Xóa sản phẩm khỏi Firestore
     */
    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage("${getString(R.string.msg_confirm_delete)}\n\n• ${product.tenSanPham}")
            .setPositiveButton(getString(R.string.dialog_delete)) { _, _ ->
                db.collection("products").document(product.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.msg_delete_success), Toast.LENGTH_SHORT).show()
                        if (editingProduct?.id == product.id) {
                            clearForm()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    // ================================================================
    //  PHẦN B — XỬ LÝ ẢNH (BASE64)
    // ================================================================

    private fun compressAndEncode(uri: Uri): String {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        val maxSize = 360
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height,
            1f
        )
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)

        val baos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 65, baos)

        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun showPreviewFromBase64(base64Str: String) {
        if (base64Str.isEmpty()) {
            binding.imgPreview.visibility = View.GONE
            return
        }
        try {
            val bytes = Base64.decode(base64Str, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) {
                binding.imgPreview.setImageBitmap(bmp)
                binding.imgPreview.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
            binding.imgPreview.visibility = View.GONE
        }
    }

    private fun getFileName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return "product_image.jpg"
    }

    // ================================================================
    //  PHẦN C — ĐĂNG NHẬP, ĐĂNG XUẤT, PHÂN QUYỀN USER
    // ================================================================

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        val email = currentUser.email ?: "user"
        binding.tvUserInfo.text = email

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                    ?: if (email.startsWith("admin", ignoreCase = true)) "admin" else "user"
                val isAdmin = role.equals("admin", ignoreCase = true)
                updateUIByRole(isAdmin)
            }
            .addOnFailureListener {
                val isAdmin = email.startsWith("admin", ignoreCase = true)
                updateUIByRole(isAdmin)
            }
    }

    private fun updateUIByRole(isAdmin: Boolean) {
        binding.apply {
            layoutForm.visibility = if (isAdmin) View.VISIBLE else View.GONE
            if (isAdmin) {
                tvRoleBadge.text = "ADMIN"
                tvRoleBadge.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.badge_admin_text))
                tvRoleBadge.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.badge_admin_bg))
            } else {
                tvRoleBadge.text = "USER (Chỉ xem)"
                tvRoleBadge.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.badge_user_text))
                tvRoleBadge.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.badge_user_bg))
            }
        }
        adapter.setAdminMode(isAdmin)
    }

    private fun logout() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // ================================================================
    //  HELPERS
    // ================================================================

    private fun validateInput(): Triple<String, String, Long>? {
        val ten = binding.edtTen.text.toString().trim()
        val loai = binding.edtLoai.text.toString().trim()
        val giaStr = binding.edtGia.text.toString().trim()

        if (ten.isEmpty() || loai.isEmpty() || giaStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_fill_all), Toast.LENGTH_SHORT).show()
            return null
        }

        val gia = giaStr.toLongOrNull()
        if (gia == null || gia <= 0) {
            Toast.makeText(this, getString(R.string.msg_invalid_price), Toast.LENGTH_SHORT).show()
            return null
        }

        return Triple(ten, loai, gia)
    }

    private fun clearForm() {
        binding.apply {
            edtTen.text?.clear()
            edtLoai.text?.clear()
            edtGia.text?.clear()
            tvImageName.text = getString(R.string.no_image)
            imgPreview.visibility = View.GONE
            btnThem.text = getString(R.string.btn_them)
            btnHuy.visibility = View.GONE
        }
        selectedImageBase64 = ""
        editingProduct = null
    }
}
